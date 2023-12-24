package ru.debajo.todos.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.onClick
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.contextClickable(enabled: Boolean, onClick: () -> Unit): Modifier {
    return onClick(
        enabled = enabled,
        matcher = PointerMatcher.mouse(PointerButton.Secondary),
        onClick = onClick,
    )
}
