package ru.debajo.todos.ui.fileconfig.model

import androidx.compose.runtime.Immutable
import ru.debajo.todos.data.storage.model.StorageFile

@Immutable
data class FileConfigState(
    val currentFile: StorageFile? = null,
    val initialLoading: Boolean = true,
    val loading: Boolean = false,
) {
    val openListButtonEnabled: Boolean = currentFile != null
}
