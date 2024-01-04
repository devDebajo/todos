package ru.debajo.todos.security

import ru.debajo.todos.auth.EncryptedPinHash
import ru.debajo.todos.auth.PinHash

interface BiometricDelegate {
    val available: Boolean

    suspend fun encryptData(rawData: String): String?

    suspend fun decryptData(encryptedData: String): String?
}

suspend fun BiometricDelegate.encryptPinHash(pinHash: PinHash): EncryptedPinHash? {
    val encrypted = encryptData(pinHash.pinHash) ?: return null
    return EncryptedPinHash(encrypted)
}
