package ru.debajo.todos.common

fun Boolean.toLong(): Long = if (this) 1L else 0L

fun Long.toBoolean(): Boolean = this == 1L

fun Long.toBooleanStrict(): Boolean {
    return when (this) {
        1L -> true
        0L -> false
        else -> error("Could not convert $this to boolean strict")
    }
}
