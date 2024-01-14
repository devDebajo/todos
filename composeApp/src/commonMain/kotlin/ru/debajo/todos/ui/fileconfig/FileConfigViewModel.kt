package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
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
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.NavigatorMediator

const val FilePinSize: Int = 6

@Stable
class FileConfigViewModel(
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
                updateState {
                    copy(files = list)
                }
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
        screenModelScope.launch {
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

    fun onFilePrimaryClick(file: StorageFile) {
        screenModelScope.launch {
            withLoading {
                val pinHash = filePinStorage.get(file)
                when (fileCodecHelper.isFileReadyToRead(file, pinHash)) {
                    FileCodecHelper.FileReadReadiness.NoPermission -> sendNews(FileConfigNews.Toast(R.strings.noReadPermission))
                    FileCodecHelper.FileReadReadiness.UnknownFormat -> sendNews(FileConfigNews.Toast(R.strings.unknownFileFormat))
                    FileCodecHelper.FileReadReadiness.Ready -> {
                        if (storageFileManager.selectFileFromList(file)) {
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
                if (fileCodecHelper.canDecryptFile(enterFilePinDialogState.file, pinHash)) {
                    updateState { copy(enterFilePinDialogState = null) }
                    storageFileManager.savePinHash(enterFilePinDialogState.file, pinHash)
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

    fun onFileSecondaryClick(file: StorageFile, position: IntOffset) {
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
            copy(filePopupMenuState = filePopupMenuState?.copy(visible = false))
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

    fun onDeleteFileConfirm() {
        val file = state.value.filePopupMenuState?.file
        hideDeleteFileDialog()
        if (file != null) {
            screenModelScope.launch {
                storageFileManager.deleteFileFromList(file)
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
