package ru.debajo.todos.common

import androidx.compose.ui.Modifier

expect fun Modifier.contextClickable(
    enabled: Boolean = true,
    onPrimaryClick: () -> Unit = { },
    onSecondaryClick: () -> Unit = { },
): Modifier
