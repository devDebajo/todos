package ru.debajo.todos.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

internal actual fun Modifier.contextClickable(
    enabled: Boolean,
    onPrimaryClick: (Offset) -> Unit,
    onSecondaryClick: (Offset) -> Unit,
): Modifier {
    TODO("Not yet implemented")
}
