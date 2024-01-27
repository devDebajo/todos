package ru.debajo.todos.security

import ru.debajo.todos.java.utils.SecureRandomUtils

actual fun randomSalt(): String = SecureRandomUtils.generateSalt()

actual fun randomIV(): ByteArray = SecureRandomUtils.generateIV()
