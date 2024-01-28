package ru.debajo.todos.java.utils

import java.nio.charset.Charset

val AppCharset: Charset = Charsets.UTF_8

fun String.toByteArrayJvm(): ByteArray = toByteArray(AppCharset)
