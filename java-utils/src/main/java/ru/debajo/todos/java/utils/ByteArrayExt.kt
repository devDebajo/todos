package ru.debajo.todos.java.utils

import java.nio.charset.Charset

fun ByteArray.toStringJvm(): String {
    return String(this, Charset.defaultCharset())
}
