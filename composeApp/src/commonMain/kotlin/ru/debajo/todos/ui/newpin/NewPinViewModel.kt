package ru.debajo.todos.ui.newpin

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.PinHasher
import ru.debajo.todos.security.encryptPinHash
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.pin.PinSize

@Stable
internal class NewPinViewModel(
    private val biometricDelegate: BiometricDelegate,
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
    private val pinHasher: PinHasher,
) : BaseNewsLessViewModel<NewPinState>(NewPinState()) {

    fun onButtonClick(symbol: Int) {
        val state = state.value
        if (state.usePin1) {
            updatePin1(symbol)
        } else {
            screenModelScope.launch {
                updatePin2(symbol)
            }
        }
    }

    private fun updatePin1(symbol: Int) {
        updateState {
            copy(pin1 = pin1 + symbol.toString(), isError = false)
        }
    }

    private suspend fun updatePin2(symbol: Int) {
        val state = state.value
        if (state.pin2.length == PinSize) {
            return
        }

        val newPin2 = state.pin2 + symbol.toString()
        updateState { copy(pin2 = newPin2, isError = false) }
        if (newPin2.length != PinSize) {
            return
        }

        delay(300)
        if (state.pin1 != newPin2) {
            updateState { copy(pin1 = "", pin2 = "", isError = true) }
            delay(600)
            updateState { copy(isError = false) }
            return
        }

        if (biometricDelegate.available) {
            updateState { copy(biometricDialogVisible = true, isError = false) }
            return
        }

        val pin = Pin(state.pin1)
        val pinHash = pinHasher.hashPin(pin)
        securityManager.configurePinAuthType(pinHash)
        navigatorMediator.replaceAll(AppScreen.SelectFile())
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
            val pinHash = pinHasher.hashPin(pin)
            val encryptedPinHash = biometricDelegate.encryptPinHash(pinHash)
            if (encryptedPinHash == null) {
                securityManager.configurePinAuthType(pinHash)
            } else {
                securityManager.configureBiometricAuthType(pinHash, encryptedPinHash)
            }
            navigatorMediator.replaceAll(AppScreen.SelectFile())
        }
    }

    fun onCancelBiometric() {
        updateState { copy(biometricDialogVisible = false) }
        screenModelScope.launch {
            val pin = Pin(state.value.pin1)
            securityManager.configurePinAuthType(pinHasher.hashPin(pin))
            navigatorMediator.replaceAll(AppScreen.SelectFile())
        }
    }
}
