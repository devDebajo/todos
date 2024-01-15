package ru.debajo.todos.common

import ru.debajo.todos.java.utils.toByteArrayJvm

actual fun String.toByteArray(): ByteArray = toByteArrayJvm()

actual fun String.formatKmp(vararg args: Any): String = format(*args)
