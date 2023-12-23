package ru.debajo.todos.common

fun Boolean.toLong(): Long = if (this) 1L else 0L

fun Long.toBoolean(): Boolean = this == 1L
