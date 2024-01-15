package ru.debajo.todos.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

internal expect fun Modifier.contextClickable(
    enabled: Boolean = true,
    onPrimaryClick: (Offset) -> Unit = { },
    onSecondaryClick: (Offset) -> Unit = { },
): Modifier
