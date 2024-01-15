package ru.debajo.todos.java.utils

import java.nio.charset.Charset

fun String.toByteArrayJvm(): ByteArray = toByteArray(Charset.defaultCharset())
