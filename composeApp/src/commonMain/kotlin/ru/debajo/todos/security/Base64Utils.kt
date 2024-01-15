package ru.debajo.todos.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import ru.debajo.todos.common.createString
import ru.debajo.todos.common.toByteArray

@OptIn(ExperimentalEncodingApi::class)
object Base64Utils {
    fun encodeString(string: String): String {
        return encode(string.toByteArray())
    }

    fun decodeString(source: String): String {
        return decode(source).createString()
    }

    fun encode(source: ByteArray): String {
        return Base64.UrlSafe.encode(source)
    }

    fun decode(source: String): ByteArray {
        return Base64.UrlSafe.decode(source)
    }
}
