package ru.debajo.todos.security

interface BiometricDelegate {
    val available: Boolean

    suspend fun encodeData(rawData: String): String?

    suspend fun decodeData(encodedData: String): String?
}
