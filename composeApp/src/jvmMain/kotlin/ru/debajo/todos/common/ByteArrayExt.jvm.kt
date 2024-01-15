package ru.debajo.todos.common

import ru.debajo.todos.java.utils.toStringJvm

actual fun ByteArray.createString(): String = toStringJvm()
