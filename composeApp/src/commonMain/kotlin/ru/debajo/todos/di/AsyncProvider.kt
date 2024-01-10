package ru.debajo.todos.di

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun interface AsyncProvider<T> {
    suspend fun provide(): T
}

fun <T, R> AsyncProvider<T>.map(transform: suspend (T) -> R): AsyncProvider<R> {
    return AsyncProvider { transform(provide()) }
}

fun <T> AsyncProvider<T>.cached(): AsyncProvider<T> {
    val receiver = this
    return if (receiver is CachedAsyncProvider<T>) {
        this
    } else {
        CachedAsyncProvider { receiver.provide() }
    }
}

private class CachedAsyncProvider<T>(
    private val factory: suspend () -> T,
) : AsyncProvider<T> {

    @Volatile
    private var value: T? = null
    private val mutex: Mutex = Mutex()

    override suspend fun provide(): T {
        return value ?: mutex.withLock {
            value ?: factory().also { value = it }
        }
    }
}
