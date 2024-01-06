package ru.debajo.todos.common

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

fun <T, R> StateFlow<T>.mapStateFlow(transform: (T) -> R): StateFlow<R> {
    val receiver = this
    return object : StateFlow<R> {
        override val replayCache: List<R>
            get() = receiver.replayCache.map(transform)

        override val value: R
            get() = transform(receiver.value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            receiver.collect { collector.emit(transform(it)) }
        }
    }
}
