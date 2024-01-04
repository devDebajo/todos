package ru.debajo.todos.security

interface BiometricDelegate {
    val available: Boolean

    suspend fun encryptData(rawData: String): String?

    suspend fun decryptData(encryptedData: String): String?
}
