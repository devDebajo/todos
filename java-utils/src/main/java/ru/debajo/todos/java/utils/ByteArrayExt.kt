package ru.debajo.todos.java.utils

fun ByteArray.toStringJvm(): String = String(this, AppCharset)
