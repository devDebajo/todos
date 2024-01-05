package ru.debajo.todos.ui.newpin

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.encryptPinHash
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.pin.PinSize

@Stable
class NewPinViewModel(
    private val biometricDelegate: BiometricDelegate,
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
) : StateScreenModel<NewPinState>(NewPinState()) {

    fun onButtonClick(symbol: Int) {
        val state = state.value
        if (state.usePin1) {
            updatePin1(symbol)
        } else {
            updatePin2(symbol)
        }
    }

    private fun updatePin1(symbol: Int) {
        updateState {
            copy(pin1 = pin1 + symbol.toString(), isError = false)
        }
    }

    private fun updatePin2(symbol: Int) {
        val state = state.value
        if (state.pin2.length == PinSize) {
            return
        }

        val newPin2 = state.pin2 + symbol.toString()
        updateState { copy(pin2 = newPin2, isError = false) }
        if (newPin2.length != PinSize) {
            return
        }

        if (state.pin1 != newPin2) {
            updateState { copy(pin2 = "", isError = true) }
            return
        }

        if (biometricDelegate.available) {
            updateState { copy(biometricDialogVisible = true, isError = false) }
            return
        }

        screenModelScope.launch {
            val pin = Pin(state.pin1)
            val pinHash = HashUtils.hashPin(pin)
            securityManager.configurePinAuthType(pinHash)
            navigatorMediator.replaceAll(AppScreen.SelectFile)
        }
    }

    fun backspace() {
        updateState {
            updateCurrentPin { pin ->
                pin.dropLast(1)
            }
        }
    }

    fun onConfirmBiometric() {
        updateState { copy(biometricDialogVisible = false) }
        screenModelScope.launch {
            val pin = Pin(state.value.pin1)
            val pinHash = HashUtils.hashPin(pin)
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
            val pin = Pin(state.value.pin1)
            securityManager.configurePinAuthType(HashUtils.hashPin(pin))
            navigatorMediator.replaceAll(AppScreen.SelectFile)
        }
    }

    private inline fun updateState(block: NewPinState.() -> NewPinState) {
        mutableState.value = mutableState.value.block()
    }
}
