package ru.debajo.todos.ui.pin

import androidx.compose.runtime.Immutable

@Immutable
data class PinState(
    val pin: String = "",
    val biometricAvailable: Boolean = false,
) {
    val actionType: ActionType = when {
        pin.isNotEmpty() -> ActionType.Backspace
        biometricAvailable -> ActionType.Biometric
        else -> ActionType.None
    }
}
