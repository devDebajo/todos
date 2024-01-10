@file:OptIn(ExperimentalEncodingApi::class)

package ru.debajo.todos.security

import java.nio.charset.Charset
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

object AesHelper {

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
    val rawBytes = rawData.toByteArray(Charset.defaultCharset())
    val encryptedBytes = encryptBytes(secret, rawBytes)
    return Base64.UrlSafe.encode(encryptedBytes)
}

fun AesHelper.decryptString(secret: String, encryptedData: String): String {
    val encryptedBytes = Base64.UrlSafe.decode(encryptedData)
    val decryptedBytes = decryptBytes(secret, encryptedBytes)
    return String(decryptedBytes, Charset.defaultCharset())
}
