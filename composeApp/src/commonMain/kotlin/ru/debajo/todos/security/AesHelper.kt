package ru.debajo.todos.security

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import ru.debajo.todos.common.createString
import ru.debajo.todos.common.toByteArray

expect object AesHelper {
    fun encryptBytes(secret: String, rawBytes: ByteArray, iv: ByteArray, salt: String): ByteArray

    fun decryptBytes(secret: String, encryptedBytes: ByteArray, iv: ByteArray, salt: String): ByteArray
}

suspend fun AesHelper.encryptStringAsync(secret: String, rawData: String, iv: IV, salt: Salt): String {
    return withContext(Default) {
        encryptString(secret, rawData, iv, salt)
    }
}

suspend fun AesHelper.decryptStringAsync(secret: String, encryptedData: String, iv: IV, salt: Salt): String {
    return withContext(Default) {
        decryptString(secret, encryptedData, iv, salt)
    }
}

fun AesHelper.encryptString(secret: String, rawData: String, iv: IV, salt: Salt): String {
    val rawBytes = rawData.toByteArray()
    val encryptedBytes = encryptBytes(secret, rawBytes, iv.bytes, salt.salt)
    return Base64Utils.encode(encryptedBytes)
}

fun AesHelper.decryptString(secret: String, encryptedData: String, iv: IV, salt: Salt): String {
    val encryptedBytes = Base64Utils.decode(encryptedData)
    val decryptedBytes = decryptBytes(secret, encryptedBytes, iv.bytes, salt.salt)
    return decryptedBytes.createString()
}
