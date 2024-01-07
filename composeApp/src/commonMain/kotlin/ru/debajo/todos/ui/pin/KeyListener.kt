package ru.debajo.todos.ui.pin

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

@Composable
expect fun KeyListener(listener: (KeyEvent) -> Boolean)

val KeyEvent.isBackspace: Boolean
    get() = key.keyCode == Key.Backspace.keyCode

val KeyEvent.number: Int?
    get() = when (key.keyCode) {
        Key.Zero.keyCode, Key.NumPad0.keyCode -> 0
        Key.One.keyCode, Key.NumPad1.keyCode -> 1
        Key.Two.keyCode, Key.NumPad2.keyCode -> 2
        Key.Three.keyCode, Key.NumPad3.keyCode -> 3
        Key.Four.keyCode, Key.NumPad4.keyCode -> 4
        Key.Five.keyCode, Key.NumPad5.keyCode -> 5
        Key.Six.keyCode, Key.NumPad6.keyCode -> 6
        Key.Seven.keyCode, Key.NumPad7.keyCode -> 7
        Key.Eight.keyCode, Key.NumPad8.keyCode -> 8
        Key.Nine.keyCode, Key.NumPad9.keyCode -> 9
        else -> null
    }
