package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import ru.debajo.todos.data.storage.model.StorageFile

@Immutable
data class FileConfigState(
    val isFilesListLoading: Boolean = true,
    val files: List<StorageFile> = emptyList(),
    val showCreateFileDialog: Boolean = false,
    val createEncryptedFileDialogState: CreateEncryptedFileDialogState? = null,
    val creatingFile: Boolean = false,
    val enterFilePinDialogState: EnterFilePinDialogState? = null,
)

@Immutable
data class EnterFilePinDialogState(
    val file: StorageFile,
    val visible: Boolean = true,
    val pin: TextFieldValue = TextFieldValue(""),
    val isError: Boolean = false,
)

@Immutable
data class CreateEncryptedFileDialogState(
    val visible: Boolean = true,
    val pin1: TextFieldValue = TextFieldValue(""),
    val pin2: TextFieldValue = TextFieldValue(""),
    val isError: Boolean = false,
) {
    fun isPinValid(requiredSize: Int): Boolean {
        if (requiredSize <= 0) {
            return false
        }
        if (pin1.text.length != requiredSize) {
            return false
        }
        if (pin2.text.length != requiredSize) {
            return false
        }
        return pin1.text == pin2.text
    }
}
