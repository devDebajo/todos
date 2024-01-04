package ru.debajo.todos.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.security.HashUtils

class AppSecurityManager(
    private val preferences: Preferences,
    appScope: CoroutineScope,
) {

    private val authType: MutableStateFlow<AuthType?> = MutableStateFlow(null)

    init {
        appScope.launch {
            authType.value = loadAuthType()
        }
    }

    suspend fun isAuthorized(): Boolean {
        val authType = getAuthType()
        if (authType == AuthType.NotConfigured) {
            return false
        }
        return true
    }

    suspend fun awaitAuthorized() {

    }

    suspend fun getAuthType(): AuthType {
        return authType.filterNotNull().first()
    }

    suspend fun getCurrentPinHash(): PinHash {
        awaitAuthorized()
        return when (getAuthType()) {
            AuthType.NotConfigured -> error("Auth type not configured")
            AuthType.Weak -> WEAK_PIN_HASH
            AuthType.Pin,
            AuthType.Biometric,
            -> TODO()
        }
    }

    suspend fun offer(pinHash: PinHash): Boolean {
        TODO()
    }

    suspend fun configureWeakAuthType() {

    }

    suspend fun configurePinAuthType(pinHash: PinHash) {

    }

    suspend fun configureBiometricAuthType(pinHash: PinHash, encryptedPinHash: EncryptedPinHash) {

    }

    private suspend fun loadAuthType(): AuthType {
        return AuthType.fromCode(preferences.getInt(AUTH_TYPE_KEY) ?: AuthType.NotConfigured.code)
    }

    private suspend fun saveAuthType(authType: AuthType) {
        preferences.putInt(AUTH_TYPE_KEY, authType.code)
    }

    private companion object {
        val WEAK_PIN: Pin = Pin("weak_pin")
        val WEAK_PIN_HASH: PinHash = PinHash(HashUtils.getHash(WEAK_PIN.pin))
        const val AUTH_TYPE_KEY: String = "AuthType"
    }
}
