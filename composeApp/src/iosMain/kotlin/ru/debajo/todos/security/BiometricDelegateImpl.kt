package ru.debajo.todos.security

internal class BiometricDelegateImpl : BiometricDelegate {
    override val available: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun encryptData(rawData: String): String? {
        TODO("Not yet implemented")
    }

    override suspend fun decryptData(encryptedData: String): String? {
        TODO("Not yet implemented")
    }
}
