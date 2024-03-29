package ru.debajo.todos.security

import ru.debajo.todos.java.utils.JvmHashUtils

actual object HashUtils {
    actual fun getHash(input: String): String = JvmHashUtils.getHash(input)
    actual fun getHash(input: String, salt: String): String = JvmHashUtils.getHash(input)
}