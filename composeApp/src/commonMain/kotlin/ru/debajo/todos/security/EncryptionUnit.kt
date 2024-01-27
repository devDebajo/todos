package ru.debajo.todos.security

expect fun randomSalt(): String

expect fun randomIV(): ByteArray

fun ByteArray.ivToString(): String {
    return joinToString(separator = ",") { it.toString() }
}

fun String.ivFromString(): ByteArray {
    return split(",").map { it.toByte() }.toByteArray()
}
