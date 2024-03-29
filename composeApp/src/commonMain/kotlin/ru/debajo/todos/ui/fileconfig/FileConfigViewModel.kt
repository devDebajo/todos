package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.BaseViewModel
import ru.debajo.todos.common.errorToNapier
import ru.debajo.todos.common.limit
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FilePinStorage
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.StorageFilesList
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.observeChanged
import ru.debajo.todos.security.PinHasher
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.NavigatorMediator

const val FilePinSize: Int = 6

@Stable
internal class FileConfigViewModel(
    private val storageFilesList: StorageFilesList,
    private val fileSelector: FileSelector,
    private val navigatorMediator: NavigatorMediator,
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val filePinStorage: FilePinStorage,
    private val fileCodecHelper: FileCodecHelper,
    private val pinHasher: PinHasher,
    private val fileHelper: FileHelper,
) : BaseViewModel<FileConfigState, FileConfigNews>(FileConfigState()) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onLaunch() {
        screenModelScope.launch {
            storageFilesList.files.filterNotNull().collect { list -> updateFiles(list) }
        }
        screenModelScope.launch {
            storageFilesList.files.filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { files -> fileHelper.observeChanged(files) }
                .collect { updateFiles(storageFilesList.files.value.orEmpty()) }
        }
    }

    fun createNewFile() {
        updateState { copy(showCreateFileDialog = true) }
    }

    fun hideCreateFileDialogs() {
        updateState {
            copy(
                showCreateFileDialog = false,
                createEncryptedFileDialogState = null,
                creatingFile = false,
            )
        }
    }

    fun selectFile() {
        screenModelScope.launch {
            val file = fileSelector.select()
            if (file != null) {
                withLoading {
                    storageFilesList.tryAddFile(file, null)
                }
            }
            hideCreateFileDialogs()
        }
    }

    fun createFile(encrypted: Boolean) {
        if (encrypted) {
            updateState {
                copy(
                    showCreateFileDialog = false,
                    createEncryptedFileDialogState = CreateEncryptedFileDialogState()
                )
            }
        } else {
            updateState {
                copy(
                    showCreateFileDialog = false,
                    creatingFile = true,
                )
            }
            screenModelScope.launch {
                val file = prepareEmptyFile(fileSelector.create(DefaultFileName))
                if (file != null) {
                    withLoading {
                        storageFilesList.tryAddFile(file, null)
                    }
                }
                hideCreateFileDialogs()
            }
        }
    }

    fun onPin1Changed(pin: TextFieldValue) {
        updateState {
            copy(
                createEncryptedFileDialogState = createEncryptedFileDialogState?.copy(
                    pin1 = pin.limit(FilePinSize),
                    isError = false,
                )
            )
        }
    }

    fun onPin2Changed(pin: TextFieldValue) {
        updateState {
            copy(
                createEncryptedFileDialogState = createEncryptedFileDialogState?.copy(
                    pin2 = pin.limit(FilePinSize),
                    isError = false
                )
            )
        }
    }

    fun onCreateFileWithEncryption() {
        val state = state.value
        val createEncryptedFileDialogState = state.createEncryptedFileDialogState
        if (createEncryptedFileDialogState == null) {
            hideCreateFileDialogs()
            return
        }

        if (state.creatingFile) {
            return
        }

        if (!createEncryptedFileDialogState.isPinValid(FilePinSize)) {
            updateState {
                copy(createEncryptedFileDialogState = createEncryptedFileDialogState.copy(isError = true))
            }
            return
        }

        updateState {
            copy(
                creatingFile = true,
                createEncryptedFileDialogState = createEncryptedFileDialogState.copy(visible = false)
            )
        }

        val pin = Pin(createEncryptedFileDialogState.pin1.text)
        screenModelScope.launch(Dispatchers.IO) {
            val pinHash = pinHasher.hashPin(pin)
            val file = prepareEmptyFile(fileSelector.create(DefaultFileName), pinHash)
            if (file != null) {
                withLoading {
                    storageFilesList.tryAddFile(file, pinHash)
                }
            }
            hideCreateFileDialogs()
        }
    }

    fun onFilePrimaryClick(file: UiStorageFile) {
        screenModelScope.launch(Dispatchers.IO) {
            withLoading {
                val domainFile = file.toStorageFile()
                val pinHash = filePinStorage.get(domainFile)
                when (fileCodecHelper.isFileReadyToRead(domainFile, pinHash)) {
                    FileCodecHelper.FileReadReadiness.NoPermission -> sendNews(FileConfigNews.Toast(R.strings.noReadPermission))
                    FileCodecHelper.FileReadReadiness.UnknownFormat -> sendNews(FileConfigNews.Toast(R.strings.unknownFileFormat))
                    FileCodecHelper.FileReadReadiness.Ready -> {
                        if (storageFilesList.selectFileFromList(domainFile)) {
                            if (databaseSnapshotSaver.load()) {
                                navigatorMediator.replaceAll(AppScreen.List)
                            } else {
                                sendNews(FileConfigNews.Toast(R.strings.someErrorWithFile))
                            }
                        } else {
                            sendNews(FileConfigNews.Toast(R.strings.someErrorWithFile))
                        }
                    }

                    FileCodecHelper.FileReadReadiness.NoPin -> {
                        updateState {
                            copy(enterFilePinDialogState = EnterFilePinDialogState(file = file))
                        }
                    }
                }
            }
        }
    }

    fun hideEnterFilePinDialog() {
        updateState {
            copy(enterFilePinDialogState = null)
        }
    }

    fun onConfirmEnterFilePinDialog() {
        val enterFilePinDialogState = state.value.enterFilePinDialogState ?: return
        if (enterFilePinDialogState.pin.text.length != FilePinSize) {
            return
        }

        val pin = Pin(enterFilePinDialogState.pin.text)
        screenModelScope.launch(Default) {
            withLoading {
                val pinHash = pinHasher.hashPin(pin)
                val domainFile = enterFilePinDialogState.file.toStorageFile()
                if (fileCodecHelper.canDecryptFile(domainFile, pinHash)) {
                    updateState { copy(enterFilePinDialogState = null) }
                    storageFilesList.savePinHash(domainFile, pinHash)
                    navigatorMediator.replaceAll(AppScreen.List)
                } else {
                    updateState {
                        copy(enterFilePinDialogState = enterFilePinDialogState.copy(isError = true))
                    }
                }
            }
        }
    }

    fun onEnterFilePinDialogPinChanged(pin: TextFieldValue) {
        updateState {
            copy(enterFilePinDialogState = enterFilePinDialogState?.copy(pin = pin.limit(FilePinSize), isError = false))
        }
    }

    fun onFileSecondaryClick(file: UiStorageFile, position: IntOffset) {
        updateState {
            copy(
                filePopupMenuState = FilePopupMenuState(
                    file = file,
                    position = position,
                )
            )
        }
    }

    fun hideFileContextPopupMenu() {
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState?.copy(
                    visible = false,
                    changeFilePinState = null,
                    showDeleteDialog = false,
                )
            )
        }
    }

    fun onDeleteFileClick() {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState.copy(
                    visible = false,
                    showDeleteDialog = true,
                )
            )
        }
    }

    fun onChangePinClick(mode: ChangeFilePinState.Mode) {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState.copy(
                    visible = false,
                    changeFilePinState = ChangeFilePinState(mode),
                )
            )
        }
    }

    fun onDeleteFileConfirm() {
        val file = state.value.filePopupMenuState?.file
        hideDeleteFileDialog()
        if (file != null) {
            screenModelScope.launch {
                storageFilesList.deleteFileFromList(file.toStorageFile())
            }
        }
    }

    fun hideDeleteFileDialog() {
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState?.copy(
                    visible = false,
                    showDeleteDialog = false,
                )
            )
        }
    }

    fun tryToAutoOpen() {
        if (state.value.autoOpenCalled) {
            return
        }
        updateState { copy(autoOpenCalled = true) }

        screenModelScope.launch {
            if (storageFilesList.isSelectLastFile()) {
                val lastFile = storageFilesList.loadLastFile()
                if (lastFile != null && storageFilesList.selectFileFromList(lastFile)) {
                    // TODO унифицировать
                    if (databaseSnapshotSaver.load()) {
                        navigatorMediator.replaceAll(AppScreen.List)
                    } else {
                        sendNews(FileConfigNews.Toast(R.strings.someErrorWithFile))
                    }
                }
            }
        }
    }

    fun onChangeFilePin1Changed(pin: TextFieldValue) {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        val changeFilePinState = filePopupMenuState.changeFilePinState ?: return
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState.copy(
                    changeFilePinState = changeFilePinState.copy(pin1 = pin.limit(FilePinSize), isError = false)
                )
            )
        }
    }

    fun onChangeFilePin2Changed(pin: TextFieldValue) {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        val changeFilePinState = filePopupMenuState.changeFilePinState ?: return
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState.copy(
                    changeFilePinState = changeFilePinState.copy(pin2 = pin.limit(FilePinSize), isError = false)
                )
            )
        }
    }

    fun onChangeFilePin3Changed(pin: TextFieldValue) {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        val changeFilePinState = filePopupMenuState.changeFilePinState ?: return
        updateState {
            copy(
                filePopupMenuState = filePopupMenuState.copy(
                    changeFilePinState = changeFilePinState.copy(pin3 = pin.limit(FilePinSize), isError = false)
                )
            )
        }
    }

    fun hideChangeFilePinDialog() {
        updateState {
            copy(filePopupMenuState = filePopupMenuState?.copy(changeFilePinState = null))
        }
    }

    fun confirmChangeFilePinDialog() {
        val filePopupMenuState = state.value.filePopupMenuState ?: return
        val changeFilePinState = filePopupMenuState.changeFilePinState ?: return

        screenModelScope.launch(Dispatchers.IO) {
            withLoading {
                val domainFile = filePopupMenuState.file.toStorageFile()
                if (changeFilePinState.validate(domainFile)) {
                    when (changeFilePinState.mode) {
                        ChangeFilePinState.Mode.AddNew -> databaseSnapshotSaver.changePin(
                            file = domainFile,
                            newPinHash = pinHasher.hashPin(Pin(changeFilePinState.pin2.text)),
                        )

                        ChangeFilePinState.Mode.Remove -> databaseSnapshotSaver.changePin(
                            file = domainFile,
                            oldPinHash = pinHasher.hashPin(Pin(changeFilePinState.pin1.text)),
                        )

                        ChangeFilePinState.Mode.Change -> databaseSnapshotSaver.changePin(
                            file = domainFile,
                            oldPinHash = pinHasher.hashPin(Pin(changeFilePinState.pin1.text)),
                            newPinHash = pinHasher.hashPin(Pin(changeFilePinState.pin2.text)),
                        )
                    }
                    hideChangeFilePinDialog()
                    updateFiles(storageFilesList.files.value.orEmpty())
                } else {
                    updateState {
                        copy(
                            filePopupMenuState = filePopupMenuState.copy(
                                changeFilePinState = changeFilePinState.copy(isError = true)
                            )
                        )
                    }
                }
            }
        }
    }

    fun openSettings() {
        screenModelScope.launch {
            navigatorMediator.navigate(AppScreen.Settings)
        }
    }

    private suspend fun prepareEmptyFile(file: StorageFile?, pinHash: PinHash? = null): StorageFile? {
        file ?: return null
        return runCatchingAsync { databaseSnapshotSaver.saveEmpty(file, pinHash) }
            .map { file }
            .getOrNull()
    }

    private suspend fun ChangeFilePinState.validate(file: StorageFile): Boolean {
        return when (mode) {
            ChangeFilePinState.Mode.AddNew -> pin2.text == pin3.text
            ChangeFilePinState.Mode.Remove -> fileCodecHelper.canDecryptFile(file, pinHasher.hashPin(Pin(pin1.text)))
            ChangeFilePinState.Mode.Change -> pin2.text == pin3.text && fileCodecHelper.canDecryptFile(file, pinHasher.hashPin(Pin(pin1.text)))
        }
    }

    private suspend fun List<StorageFile>.convert(): ConvertResult {
        return withContext(Dispatchers.IO) {
            val invalidFiles = mutableListOf<StorageFile>()
            val successFiles = mutableListOf<UiStorageFile>()

            for (domainFile in this@convert) {
                val fileMeta = domainFile.getFileMeta()
                if (fileMeta != null) {
                    successFiles += UiStorageFile(
                        name = domainFile.name,
                        extension = domainFile.extension,
                        absolutePath = domainFile.absolutePath,
                        encrypted = fileMeta.encrypted,
                        edited = fileMeta.edited,
                    )
                } else {
                    invalidFiles.add(domainFile)
                }
            }

            ConvertResult(
                success = successFiles,
                failed = invalidFiles
            )
        }
    }

    private suspend fun StorageFile.getFileMeta(): UiFileMeta? {
        val encrypted = runCatchingAsync { fileCodecHelper.isEncrypted(this) }
            .errorToNapier("isEncrypted error")
            .getOrNull() ?: return null

        val edited = runCatchingAsync {
            if (encrypted) {
                fileCodecHelper.getTimestamp(this, filePinStorage.get(this))
            } else {
                fileCodecHelper.getTimestamp(this, null)
            }
        }
            .mapCatching { Instant.fromEpochMilliseconds(it) }
            .errorToNapier("getTimestamp error")
            .getOrNull() ?: return null
        return UiFileMeta(encrypted, edited)
    }

    private class UiFileMeta(val encrypted: Boolean, val edited: Instant)

    private suspend fun removeFromList(files: List<StorageFile>) {
        for (file in files) {
            storageFilesList.deleteFileFromList(file)
        }
    }

    private suspend fun withLoading(block: suspend () -> Unit) {
        updateState { copy(isLoading = true) }
        try {
            block()
        } finally {
            updateState { copy(isLoading = false) }
        }
    }

    private suspend fun updateFiles(files: List<StorageFile>) {
        val convertResult = files.convert()
        removeFromList(convertResult.failed)
        updateState { copy(files = convertResult.success) }
    }

    private class ConvertResult(
        val success: List<UiStorageFile>,
        val failed: List<StorageFile>,
    )

    private companion object {
        const val DefaultFileName: String = "todos.tds"
    }
}
