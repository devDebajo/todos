package ru.debajo.todos.security

import ru.debajo.todos.java.utils.JvmAesHelper

actual object AesHelper {
    actual fun encryptBytes(secret: String, rawBytes: ByteArray): ByteArray {
        return JvmAesHelper.encryptBytes(secret, rawBytes)
    }

    actual fun decryptBytes(secret: String, encryptedBytes: ByteArray): ByteArray {
        return JvmAesHelper.decryptBytes(secret, encryptedBytes)
    }
}
