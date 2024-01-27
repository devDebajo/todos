package ru.debajo.todos.security

expect fun randomIV(): ByteArray

expect fun randomSalt(): String

fun generateIV(): IV = IV(randomIV())

fun generateSalt(): Salt = Salt(randomSalt())

fun IV.ivToString(): String {
    return bytes.joinToString(separator = ",") { it.toString() }
}

fun String.ivFromString(): IV {
    return IV(split(",").map { it.toByte() }.toByteArray())
}
