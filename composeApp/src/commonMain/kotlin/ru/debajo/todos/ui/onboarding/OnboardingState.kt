package ru.debajo.todos.ui.onboarding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import ru.debajo.todos.auth.Pin

@Immutable
data class OnboardingState(
    val weakAuthTypeWarningDialogVisible: Boolean = false,

    val enterPinDialogVisible: Boolean = false,
    val enterPinDialogError: Boolean = false,
    val pin: TextFieldValue = TextFieldValue(""),
    val pinConfirmation: TextFieldValue = TextFieldValue(""),

    val biometricDialogVisible: Boolean = false,
) {
    val savePinButtonEnabled: Boolean = pin.text.isNotEmpty() && pinConfirmation.text.isNotEmpty()
    val currentPin: Pin
        get() = Pin(pin.text)
}
