package ru.debajo.todos.ui.fileconfig.model

import androidx.compose.runtime.Immutable
import ru.debajo.todos.data.storage.model.StorageFile

@Immutable
@Deprecated("")
data class FileConfigState(
    val currentFile: StorageFile? = null,
    val initialLoading: Boolean = false,
    val loading: Boolean = false,
) {
    val openListButtonEnabled: Boolean = currentFile != null
}
