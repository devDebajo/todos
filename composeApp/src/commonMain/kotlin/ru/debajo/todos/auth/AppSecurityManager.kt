package ru.debajo.todos.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.security.AesHelper
import ru.debajo.todos.security.EncryptedPinHash
import ru.debajo.todos.security.HashUtils

class AppSecurityManager(
    private val preferences: Preferences,
    appScope: CoroutineScope,
) {

    private val currentHash: MutableStateFlow<PinHash?> = MutableStateFlow(null)
    private val authType: MutableStateFlow<AuthType?> = MutableStateFlow(null)

    init {
        appScope.launch {
            val authType = loadAuthType()
            this@AppSecurityManager.authType.value = authType
            if (authType == AuthType.Weak) {
                currentHash.value = WEAK_PIN_HASH
            }
        }
    }

    suspend fun isAuthorized(): Boolean {
        val authType = getAuthType()
        return if (authType == AuthType.NotConfigured) {
            false
        } else {
            currentHash.value != null
        }
    }

    suspend fun awaitAuthorized() {
        currentHash.filterNotNull().first()
    }

    suspend fun getAuthType(): AuthType {
        return authType.filterNotNull().first()
    }

    suspend fun getCurrentPinHash(): PinHash {
        return when (getAuthType()) {
            AuthType.NotConfigured -> error("Auth type not configured")
            AuthType.Weak -> WEAK_PIN_HASH
            AuthType.Pin, AuthType.Biometric -> currentHash.filterNotNull().first()
        }
    }

    suspend fun offer(pinHash: PinHash): Boolean {
        return if (isHashValid(pinHash)) {
            currentHash.value = pinHash
            true
        } else {
            false
        }
    }

    suspend fun configureWeakAuthType() {
        saveAuthType(AuthType.Weak)
        currentHash.value = WEAK_PIN_HASH
        clearPinHashHack()
        clearEncryptedPinHash()
    }

    suspend fun configurePinAuthType(pinHash: PinHash) {
        saveAuthType(AuthType.Pin)
        currentHash.value = pinHash
        savePinHashHack(pinHash)
        clearEncryptedPinHash()
    }

    suspend fun configureBiometricAuthType(pinHash: PinHash, encryptedPinHash: EncryptedPinHash) {
        saveAuthType(AuthType.Biometric)
        currentHash.value = pinHash
        savePinHashHack(pinHash)
        saveEncryptedPinHash(encryptedPinHash)
    }

    suspend fun useBiometric(decryptor: suspend (EncryptedPinHash) -> PinHash?): Boolean {
        val encryptedPinHash = loadEncryptedPinHash() ?: return false
        val hash = decryptor(encryptedPinHash) ?: return false
        return offer(hash)
    }

    private suspend fun loadEncryptedPinHash(): EncryptedPinHash? {
        val encryptedPinHash = preferences.getString(ENCRYPTED_HASH_KEY) ?: return null
        return EncryptedPinHash(encryptedPinHash)
    }

    private suspend fun loadAuthType(): AuthType {
        return AuthType.fromCode(preferences.getInt(AUTH_TYPE_KEY) ?: AuthType.NotConfigured.code)
    }

    private suspend fun saveAuthType(authType: AuthType) {
        this@AppSecurityManager.authType.value = authType
        preferences.putInt(AUTH_TYPE_KEY, authType.code)
    }

    private suspend fun clearPinHashHack() {
        preferences.remove(PIN_HASH_HACK_KEY)
    }

    private suspend fun savePinHashHack(pinHash: PinHash) {
        val encryptedHack = AesHelper.encrypt(pinHash.pinHash, PIN_HASH_HACK)
        preferences.putString(PIN_HASH_HACK_KEY, encryptedHack)
    }

    private suspend fun isHashValid(pinHash: PinHash): Boolean {
        val encryptedHackFromPrefs = preferences.getString(PIN_HASH_HACK_KEY) ?: return false
        val encryptedHack = AesHelper.encrypt(pinHash.pinHash, PIN_HASH_HACK)
        return encryptedHackFromPrefs == encryptedHack
    }

    private suspend fun saveEncryptedPinHash(encryptedPinHash: EncryptedPinHash) {
        preferences.putString(ENCRYPTED_HASH_KEY, encryptedPinHash.encryptedPinHash)
    }

    private suspend fun clearEncryptedPinHash() {
        preferences.remove(ENCRYPTED_HASH_KEY)
    }

    private companion object {
        val WEAK_PIN: Pin = Pin("weak_pin")
        val WEAK_PIN_HASH: PinHash = HashUtils.hashPin(WEAK_PIN)
        const val PIN_HASH_HACK: String = "PIN_HASH_HACK" // do not change
        const val AUTH_TYPE_KEY: String = "at"
        const val PIN_HASH_HACK_KEY: String = "phh"
        const val ENCRYPTED_HASH_KEY: String = "eh"
    }
}
