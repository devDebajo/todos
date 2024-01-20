package ru.debajo.todos.ui.pin

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.auth.AuthType
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.hashPin
import ru.debajo.todos.ui.NavigatorMediator

@Stable
internal class PinViewModel(
    private val biometricDelegate: BiometricDelegate,
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
) : BaseNewsLessViewModel<PinState>(PinState()) {

    override fun onLaunch() {
        screenModelScope.launch {
            val authType = securityManager.getAuthType()
            updateState {
                copy(biometricAvailable = authType == AuthType.Biometric && biometricDelegate.available)
            }
            showBiometric()
        }
    }

    fun showBiometric() {
        if (state.value.biometricAvailable) {
            screenModelScope.launch {
                val used = securityManager.useBiometric { encrypted ->
                    biometricDelegate.decryptData(encrypted.encryptedPinHash)?.let { PinHash(it) }
                }

                if (used) {
                    navigatorMediator.replaceAll(AppScreen.SelectFile(true))
                }
            }
        }
    }

    fun onButtonClick(symbol: Int) {
        if (state.value.pin.length == PinSize) {
            return
        }

        val newPin = state.value.pin + symbol.toString()
        updateState { copy(pin = newPin, isError = false) }
        if (newPin.length == PinSize) {
            screenModelScope.launch(Default) {
                val pin = Pin(state.value.pin)
                val pinHash = HashUtils.hashPin(pin)
                if (securityManager.offer(pinHash)) {
                    navigatorMediator.replaceAll(AppScreen.SelectFile(true))
                } else {
                    updateState { copy(pin = "", isError = true) }
                }
            }
        }
    }

    fun backspace() {
        updateState { copy(pin = pin.dropLast(1), isError = false) }
    }
}
