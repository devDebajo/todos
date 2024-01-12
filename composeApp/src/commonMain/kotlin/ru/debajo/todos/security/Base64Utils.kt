package ru.debajo.todos.security

import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
object Base64Utils {
    fun encodeString(string: String): String {
        return encode(string.toByteArray(Charset.defaultCharset()))
    }

    fun decodeString(source: String): String {
        return String(decode(source), Charset.defaultCharset())
    }

    fun encode(source: ByteArray): String {
        return Base64.UrlSafe.encode(source)
    }

    fun decode(source: String): ByteArray {
        return Base64.UrlSafe.decode(source)
    }
}
