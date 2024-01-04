package ru.debajo.todos.ui.onboarding

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.encryptPinHash
import ru.debajo.todos.ui.AppScreen
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.pin.PinSize

@Stable
class OnboardingViewModel(
    private val biometricDelegate: BiometricDelegate,
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
) : StateScreenModel<OnboardingState>(OnboardingState()) {

    fun onPinClick() {
        updateState {
            resetPinDialog().copy(
                enterPinDialogVisible = true,
                weakAuthTypeWarningDialogVisible = false,
            )
        }
    }

    fun onWeakClick(force: Boolean) {
        if (force) {
            updateState {
                resetPinDialog().copy(weakAuthTypeWarningDialogVisible = false)
            }
            screenModelScope.launch {
                securityManager.configureWeakAuthType()
                navigatorMediator.replaceAll(AppScreen.SelectFile)
            }
        } else {
            updateState {
                resetPinDialog().copy(weakAuthTypeWarningDialogVisible = true)
            }
        }
    }

    fun onPinChanged(pin: TextFieldValue) {
        updateState {
            copy(
                pin = pin.copy(text = pin.text.take(PinSize)),
                enterPinDialogError = false,
            )
        }
    }

    fun onPinConfirmationChanged(pin: TextFieldValue) {
        updateState {
            copy(
                pinConfirmation = pin.copy(text = pin.text.take(PinSize)),
                enterPinDialogError = false
            )
        }
    }

    fun onCancelPin() {
        updateState {
            resetPinDialog()
        }
    }

    fun onConfirmPin() {
        val state = state.value
        if (state.pin.text.isEmpty() || state.pinConfirmation.text.isEmpty()) {
            updateState { copy(enterPinDialogError = true) }
            return
        }

        if (state.pin.text != state.pinConfirmation.text) {
            updateState { copy(enterPinDialogError = true) }
        } else {
            if (biometricDelegate.available) {
                updateState {
                    copy(
                        enterPinDialogVisible = false,
                        biometricDialogVisible = true,
                    )
                }
            } else {
                screenModelScope.launch {
                    securityManager.configurePinAuthType(HashUtils.hashPin(state.currentPin))
                    navigatorMediator.replaceAll(AppScreen.SelectFile)
                }
            }
        }
    }

    fun onConfirmBiometric() {
        updateState { copy(biometricDialogVisible = false) }
        screenModelScope.launch {
            val pinHash = HashUtils.hashPin(state.value.currentPin)
            val encryptedPinHash = biometricDelegate.encryptPinHash(pinHash)
            if (encryptedPinHash == null) {
                securityManager.configurePinAuthType(pinHash)
            } else {
                securityManager.configureBiometricAuthType(pinHash, encryptedPinHash)
            }
            navigatorMediator.replaceAll(AppScreen.SelectFile)
        }
    }

    fun onCancelBiometric() {
        updateState { copy(biometricDialogVisible = false) }
        screenModelScope.launch {
            securityManager.configurePinAuthType(HashUtils.hashPin(state.value.currentPin))
            navigatorMediator.replaceAll(AppScreen.SelectFile)
        }
    }

    private fun OnboardingState.resetPinDialog(): OnboardingState {
        return copy(
            enterPinDialogError = false,
            enterPinDialogVisible = false,
            pin = TextFieldValue(""),
            pinConfirmation = TextFieldValue(""),
        )
    }

    private inline fun updateState(block: OnboardingState.() -> OnboardingState) {
        mutableState.value = mutableState.value.block()
    }
}
