package ru.debajo.todos.java.utils

import java.security.MessageDigest

// https://www.baeldung.com/sha-256-hashing-java
object JvmHashUtils {
    fun getHash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(input.toByteArrayJvm())
        return bytesToHex(digest)
    }

    fun getHash(input: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(salt.toByteArrayJvm())
        val digest = messageDigest.digest(input.toByteArrayJvm())
        return bytesToHex(digest)
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
