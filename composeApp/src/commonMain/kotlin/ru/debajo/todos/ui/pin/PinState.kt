package ru.debajo.todos.ui.pin

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue

@Immutable
data class PinState(
    val pin: TextFieldValue = TextFieldValue(""),
    val showBiometricButton: Boolean = false,
)
