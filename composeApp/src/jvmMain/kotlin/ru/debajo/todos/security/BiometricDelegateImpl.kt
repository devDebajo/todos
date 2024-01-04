package ru.debajo.todos.security

internal object BiometricDelegateImpl : BiometricDelegate {
    override val available: Boolean = false
    override suspend fun encodeData(rawData: String): String? = null
    override suspend fun decodeData(encodedData: String): String? = null
}
