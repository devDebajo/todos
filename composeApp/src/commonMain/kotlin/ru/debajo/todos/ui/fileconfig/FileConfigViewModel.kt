package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.common.BaseViewModel
import ru.debajo.todos.common.limit
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.FilePinStorage
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.StorageFileManager
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.hashPin
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.NavigatorMediator

const val FilePinSize: Int = 6

@Stable
internal class FileConfigViewModel(
    private val storageFileManager: StorageFileManager,
    private val fileSelector: FileSelector,
    private val navigatorMediator: NavigatorMediator,
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val filePinStorage: FilePinStorage,
    private val fileCodecHelper: FileCodecHelper,
) : BaseViewModel<FileConfigState, FileConfigNews>(FileConfigState()) {

    override fun onLaunch() {
        screenModelScope.launch {
            storageFileManager.files.filterNotNull().collect { list ->
                val uiFiles = list.convert()
                updateState { copy(files = uiFiles) }
            }
        }
        screenModelScope.launch {
            val isSelectLastFile = storageFileManager.isSelectLastFile()
            updateState {
                copy(isAutoOpenLastFile = isSelectLastFile)
            }
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
                    storageFileManager.tryAddFile(file, null)
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
                val file = fileSelector.create(DefaultFileName)
                if (file != null) {
                    withLoading {
                        storageFileManager.tryAddFile(file, null)
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
            val file = fileSelector.create(DefaultFileName)
            if (file != null) {
                withLoading {
                    val pinHash = HashUtils.hashPin(pin)
                    storageFileManager.tryAddFile(file, pinHash)
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
                        if (storageFileManager.selectFileFromList(domainFile)) {
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
                val pinHash = HashUtils.hashPin(pin)
                val domainFile = enterFilePinDialogState.file.toStorageFile()
                if (fileCodecHelper.canDecryptFile(domainFile, pinHash)) {
                    updateState { copy(enterFilePinDialogState = null) }
                    storageFileManager.savePinHash(domainFile, pinHash)
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
                storageFileManager.deleteFileFromList(file.toStorageFile())
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

    fun onAutoOpenSwitchChanged(value: Boolean) {
        updateState {
            copy(isAutoOpenLastFile = value)
        }

        screenModelScope.launch {
            storageFileManager.setSelectLastFile(value)
        }
    }

    fun tryToAutoOpen() {
        screenModelScope.launch {
            if (storageFileManager.isSelectLastFile()) {
                val lastFile = storageFileManager.loadLastFile()
                if (lastFile != null && storageFileManager.selectFileFromList(lastFile)) {
                    navigatorMediator.replaceAll(AppScreen.List)
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
                            newPinHash = HashUtils.hashPin(Pin(changeFilePinState.pin2.text)),
                        )

                        ChangeFilePinState.Mode.Remove -> databaseSnapshotSaver.changePin(
                            file = domainFile,
                            oldPinHash = HashUtils.hashPin(Pin(changeFilePinState.pin1.text)),
                        )

                        ChangeFilePinState.Mode.Change -> databaseSnapshotSaver.changePin(
                            file = domainFile,
                            oldPinHash = HashUtils.hashPin(Pin(changeFilePinState.pin1.text)),
                            newPinHash = HashUtils.hashPin(Pin(changeFilePinState.pin2.text)),
                        )
                    }
                    hideChangeFilePinDialog()
                    val uiFiles = storageFileManager.files.value.orEmpty().convert()
                    updateState { copy(files = uiFiles) }
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

    private suspend fun ChangeFilePinState.validate(file: StorageFile): Boolean {
        return when (mode) {
            ChangeFilePinState.Mode.AddNew -> pin2.text == pin3.text
            ChangeFilePinState.Mode.Remove -> fileCodecHelper.canDecryptFile(file, HashUtils.hashPin(Pin(pin1.text)))
            ChangeFilePinState.Mode.Change -> pin2.text == pin3.text && fileCodecHelper.canDecryptFile(file, HashUtils.hashPin(Pin(pin1.text)))
        }
    }

    private suspend fun List<StorageFile>.convert(): List<UiStorageFile> {
        return withContext(Dispatchers.IO) {
            map { domainFile ->
                UiStorageFile(
                    name = domainFile.name,
                    extension = domainFile.extension,
                    absolutePath = domainFile.absolutePath,
                    encrypted = fileCodecHelper.isEncrypted(domainFile)
                )
            }
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

    private companion object {
        const val DefaultFileName: String = "todos.tds"
    }
}
