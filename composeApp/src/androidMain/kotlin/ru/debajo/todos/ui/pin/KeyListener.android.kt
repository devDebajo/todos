package ru.debajo.todos.ui.pin

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.KeyEvent

@Composable
actual fun KeyListener(listener: (KeyEvent) -> Boolean) = Unit
