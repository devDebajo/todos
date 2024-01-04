package ru.debajo.todos.ui.newpin

import androidx.compose.runtime.Immutable
import ru.debajo.todos.ui.pin.ActionType
import ru.debajo.todos.ui.pin.PinSize

@Immutable
data class NewPinState(
    val pin1: String = "",
    val pin2: String = "",
    val isError: Boolean = false,
    val biometricDialogVisible: Boolean = false,
) {
    val usePin1: Boolean = pin1.length < PinSize
    val currentPin: String = if (usePin1) pin1 else pin2
    val actionType: ActionType = if (currentPin.isNotEmpty()) {
        ActionType.Backspace
    } else {
        ActionType.None
    }
}

inline fun NewPinState.updateCurrentPin(block: (String) -> String): NewPinState {
    return if (usePin1) {
        copy(pin1 = block(pin1))
    } else {
        copy(pin2 = block(pin2))
    }
}
