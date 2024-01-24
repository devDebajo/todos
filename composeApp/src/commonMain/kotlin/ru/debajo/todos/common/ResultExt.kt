package ru.debajo.todos.common

import io.github.aakira.napier.Napier

fun <R> Result<R>.errorToNapier(message: String): Result<R> {
    return onFailure { Napier.e(message, it) }
}
