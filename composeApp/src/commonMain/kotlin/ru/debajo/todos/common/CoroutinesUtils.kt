package ru.debajo.todos.common

import kotlinx.coroutines.CancellationException

internal inline fun <T> runCatchingAsync(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
