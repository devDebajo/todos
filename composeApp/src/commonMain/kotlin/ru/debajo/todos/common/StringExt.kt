package ru.debajo.todos.common

fun String.ellipsize(limit: Int): String {
    if (length <= limit) {
        return this
    }

    return take(limit) + "…"
}
