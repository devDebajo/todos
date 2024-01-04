package ru.debajo.todos.security

internal object BiometricDelegateImpl : BiometricDelegate {
    override val available: Boolean = false
    override suspend fun encryptData(rawData: String): String? = null
    override suspend fun decryptData(encryptedData: String): String? = null
}
