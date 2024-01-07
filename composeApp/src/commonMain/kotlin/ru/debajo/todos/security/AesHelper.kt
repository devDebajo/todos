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

@OptIn(ExperimentalEncodingApi::class)
object AesHelper {

    private val keyCache: ConcurrentHashMap<String, SecretKey> = ConcurrentHashMap()

    suspend fun encrypt(secret: String, rawData: String): String {
        return withContext(Default) {
            val secretKey = createKey(secret)
            val cipher = createCipher()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IV)
            val encryptedBytes = cipher.doFinal(rawData.toByteArray(Charset.defaultCharset()))
            Base64.UrlSafe.encode(encryptedBytes)
        }
    }

    suspend fun decrypt(secret: String, encryptedData: String): String {
        return withContext(Default) {
            val secretKey = createKey(secret)
            val cipher = createCipher()
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IV)
            val encryptedBytes = Base64.UrlSafe.decode(encryptedData)
            String(cipher.doFinal(encryptedBytes), Charset.defaultCharset())
        }
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

