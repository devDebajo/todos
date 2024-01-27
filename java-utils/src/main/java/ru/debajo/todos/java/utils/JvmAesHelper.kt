package ru.debajo.todos.java.utils

import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object JvmAesHelper {

    const val blockSize: Int = 16
    private val keyCache: ConcurrentHashMap<String, SecretKey> = ConcurrentHashMap()

    fun encryptBytes(secret: String, rawBytes: ByteArray, iv: ByteArray = IV, salt: String = SALT): ByteArray {
        val secretKey = createKey(secret, salt)
        val cipher = createCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(rawBytes)
    }

    fun decryptBytes(secret: String, encryptedBytes: ByteArray, iv: ByteArray = IV, salt: String = SALT): ByteArray {
        val secretKey = createKey(secret, salt)
        val cipher = createCipher()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(encryptedBytes)
    }

    private fun createKey(secret: String, salt: String): SecretKey {
        return keyCache.getOrPut(secret + salt) {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(secret.toCharArray(), salt.toByteArray(Charset.defaultCharset()), 65536, 256)
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }
    }

    private fun createCipher(): Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    private val IV: ByteArray = byteArrayOf(-44, -1, -65, -68, 61, 67, -37, 94, 122, 24, -48, 77, 72, 116, 115, 6)
    private const val SALT: String = "hjg4gh5j43fgfuiyfdsf564816723tghvghf65f678astgy43i25grjkh"
}
