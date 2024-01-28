package ru.debajo.todos.ui.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsState(
    val isAutoOpenLastFile: Boolean = false,
)
