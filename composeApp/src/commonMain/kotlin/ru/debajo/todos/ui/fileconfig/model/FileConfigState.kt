package ru.debajo.todos.ui.fileconfig.model

import androidx.compose.runtime.Immutable

@Immutable
data class FileConfigState(
    val currentFileUri: String? = null,
    val loading: Boolean = false,
) {
    val openListButtonEnabled: Boolean = currentFileUri != null
}
