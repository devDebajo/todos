package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset

@Immutable
data class FileConfigState(
    val isLoading: Boolean = false,
    val files: List<UiStorageFile>? = null,
    val showCreateFileDialog: Boolean = false,
    val createEncryptedFileDialogState: CreateEncryptedFileDialogState? = null,
    val creatingFile: Boolean = false,
    val enterFilePinDialogState: EnterFilePinDialogState? = null,
    val filePopupMenuState: FilePopupMenuState? = null,
    val showAboutDialog: Boolean = false,
) {
    val showBlockingLoading: Boolean = files == null || isLoading
}

@Immutable
data class FilePopupMenuState(
    val file: UiStorageFile,
    val position: IntOffset,
    val visible: Boolean = true,
    val changeFilePinState: ChangeFilePinState? = null,
    val showDeleteDialog: Boolean = false,
)

@Immutable
data class ChangeFilePinState(
    val mode: Mode,
    val pin1: TextFieldValue = TextFieldValue(""),
    val pin2: TextFieldValue = TextFieldValue(""),
    val pin3: TextFieldValue = TextFieldValue(""),
    val isError: Boolean = false,
) {
    enum class Mode { AddNew, Remove, Change }
}

@Immutable
data class EnterFilePinDialogState(
    val file: UiStorageFile,
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
