package ru.debajo.todos.security

import ru.debajo.todos.java.utils.JvmAesHelper

actual object AesHelper {
    actual fun encryptBytes(secret: String, rawBytes: ByteArray, iv: ByteArray, salt: String): ByteArray {
        return JvmAesHelper.encryptBytes(secret, rawBytes, iv, salt)
    }

    actual fun decryptBytes(secret: String, encryptedBytes: ByteArray, iv: ByteArray, salt: String): ByteArray {
        return JvmAesHelper.decryptBytes(secret, encryptedBytes, iv, salt)
    }
}
