package ru.debajo.todos.security

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import ru.debajo.todos.common.createString
import ru.debajo.todos.common.toByteArray

expect object AesHelper {
    fun encryptBytes(secret: String, rawBytes: ByteArray): ByteArray

    fun decryptBytes(secret: String, encryptedBytes: ByteArray): ByteArray
}

suspend fun AesHelper.encryptStringAsync(secret: String, rawData: String): String {
    return withContext(Default) {
        encryptString(secret, rawData)
    }
}

suspend fun AesHelper.decryptStringAsync(secret: String, encryptedData: String): String {
    return withContext(Default) {
        decryptString(secret, encryptedData)
    }
}

fun AesHelper.encryptString(secret: String, rawData: String): String {
    val rawBytes = rawData.toByteArray()
    val encryptedBytes = encryptBytes(secret, rawBytes)
    return Base64Utils.encode(encryptedBytes)
}

fun AesHelper.decryptString(secret: String, encryptedData: String): String {
    val encryptedBytes = Base64Utils.decode(encryptedData)
    val decryptedBytes = decryptBytes(secret, encryptedBytes)
    return decryptedBytes.createString()
}
