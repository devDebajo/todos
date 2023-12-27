package ru.debajo.todos.ui.fileconfig.model

import androidx.compose.runtime.Immutable

@Immutable
data class FileConfigState(
    val currentFileUri: String? = null,
    val initialLoading: Boolean = true,
    val loading: Boolean = false,
) {
    val openListButtonEnabled: Boolean = currentFileUri != null
}
