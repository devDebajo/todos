package ru.debajo.todos.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.contextClickable(enabled: Boolean, onClick: () -> Unit): Modifier {
    return combinedClickable(
        enabled = enabled,
        onLongClick = onClick,
        onClick = {},
    )
}
