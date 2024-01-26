package ru.debajo.todos.security

import ru.debajo.todos.java.utils.JvmAesHelper

actual fun randomSalt(): String = JvmAesHelper.generateSalt()

actual fun randomIV(): ByteArray = JvmAesHelper.generateIV()
