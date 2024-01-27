package ru.debajo.todos.security

actual object AesHelper {
    actual fun encryptBytes(secret: String, rawBytes: ByteArray, iv: ByteArray, salt: String): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun decryptBytes(secret: String, encryptedBytes: ByteArray, iv: ByteArray, salt: String): ByteArray {
        TODO("Not yet implemented")
    }
}
