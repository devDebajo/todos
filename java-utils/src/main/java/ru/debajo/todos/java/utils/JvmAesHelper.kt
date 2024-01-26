package ru.debajo.todos.java.utils

import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object JvmAesHelper {

    private const val blockSize: Int = 16
    private val secureRandom: SecureRandom by lazy { SecureRandom() }
    private val keyCache: ConcurrentHashMap<String, SecretKey> = ConcurrentHashMap()

    fun encryptBytes(secret: String, rawBytes: ByteArray): ByteArray {
        val secretKey = createKey(secret)
        val cipher = createCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV)
        return cipher.doFinal(rawBytes)
    }

    fun decryptBytes(secret: String, encryptedBytes: ByteArray): ByteArray {
        val secretKey = createKey(secret)
        val cipher = createCipher()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IV)
        return cipher.doFinal(encryptedBytes)
    }

    fun generateIV(blockSize: Int = this.blockSize): ByteArray {
        val result = ByteArray(blockSize)
        secureRandom.nextBytes(result)
        return result
    }

    fun generateSalt(size: Int = 30): String {
        val builder = StringBuilder()
        repeat(size) {
            builder.append(secureRandom.nextInt('0'.code, 'z'.code).toChar())
        }
        return builder.toString()
    }

    private fun createKey(secret: String): SecretKey {
        return keyCache.getOrPut(secret) {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(secret.toCharArray(), SALT.toByteArray(Charset.defaultCharset()), 65536, 256)
            SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }
    }

    private fun createCipher(): Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

    private val IV: IvParameterSpec = IvParameterSpec(byteArrayOf(-44, -1, -65, -68, 61, 67, -37, 94, 122, 24, -48, 77, 72, 116, 115, 6))
    private const val SALT: String = "hjg4gh5j43fgfuiyfdsf564816723tghvghf65f678astgy43i25grjkh"
}
