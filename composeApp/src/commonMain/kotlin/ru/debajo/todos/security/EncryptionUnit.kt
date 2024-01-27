package ru.debajo.todos.security

expect fun randomIV(): ByteArray

expect fun randomSalt(): String

fun generateIV(): IV = IV(randomIV())

fun generateSalt(): Salt = Salt(randomSalt())

fun IV.ivToString(): String {
    return bytes.joinToString(separator = ",") { it.toString() }
}

val IV.Companion.Empty: IV
    get() = IV(EmptyByteArray)

private val EmptyByteArray: ByteArray = byteArrayOf()

fun String.ivFromString(): IV {
    if (isEmpty()) {
        return IV.Empty
    }
    return IV(split(",").map { it.toByte() }.toByteArray())
}
