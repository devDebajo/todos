package ru.debajo.todos.security

import java.nio.charset.Charset
import java.security.MessageDigest
import ru.debajo.todos.auth.Pin
import ru.debajo.todos.auth.PinHash

// https://www.baeldung.com/sha-256-hashing-java
object HashUtils {
    fun getHash(input: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(SALT.toByteArray(Charset.defaultCharset()))
        val digest = messageDigest.digest(input.toByteArray(Charset.defaultCharset()))
        return bytesToHex(digest)
    }

    fun hashPin(pin: Pin): PinHash = PinHash(getHash(pin.pin))

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
