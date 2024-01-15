package ru.debajo.todos.java.utils

import java.security.MessageDigest

// https://www.baeldung.com/sha-256-hashing-java
object JvmHashUtils {
    fun getHash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(SALT.toByteArrayJvm())
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

    private const val SALT: String = "hfdjhkj345hjkg534jhkgryfdrt78sa5678dsatfg3jhgdsfagfiusgadjhygr3uy24t32768gtdsytfysa"
}
