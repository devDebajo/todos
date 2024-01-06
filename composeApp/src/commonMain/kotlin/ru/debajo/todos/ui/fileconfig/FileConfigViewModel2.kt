package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.common.limit
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.StorageFileManager
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.security.HashUtils

const val FilePinSize: Int = 6

@Stable
class FileConfigViewModel2(
    private val storageFileManager: StorageFileManager,
    private val fileSelector: FileSelector,
) : BaseNewsLessViewModel<FileConfigState2>(FileConfigState2()) {

    override fun onLaunch() {
        screenModelScope.launch {
            storageFileManager.files.filterNotNull().collect { list ->
                updateState {
                    copy(files = list, isFilesListLoading = false)
                }
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
                val file = fileSelector.create("todos", StorageFile.NotEncryptedExtension)
                if (file != null) {
                    storageFileManager.tryAddFile(file, null)
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
            val file = fileSelector.create("todos", StorageFile.EncryptedExtension)
            if (file != null) {
                val pinHash = HashUtils.hashPin(pin)
                storageFileManager.tryAddFile(file, pinHash)
            }
            hideCreateFileDialogs()
        }
    }

    fun onFilePrimaryClick(file: StorageFile) {
    }

    fun onFileSecondaryClick(file: StorageFile) {
    }
}
