package ru.debajo.todos.common

import androidx.compose.ui.Modifier

expect fun Modifier.contextClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier
