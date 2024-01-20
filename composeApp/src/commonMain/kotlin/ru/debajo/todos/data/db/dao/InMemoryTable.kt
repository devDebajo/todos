package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryTable<T>(private val onUpdate: suspend () -> Unit) {

    private val entries: MutableStateFlow<List<T>> = MutableStateFlow(emptyList())
    private val mutex: Mutex = Mutex()

    suspend fun insertOrReplace(entry: T, keyGetter: (T) -> String): Unit = insertOrReplace(listOf(entry), keyGetter)

    suspend fun insertOrReplace(entries: List<T>, keyGetter: (T) -> String) {
        updateRaw { currentValue ->
            val newEntriesKeys = entries.map { keyGetter(it) }.toSet()
            currentValue
                .filter { keyGetter(it) !in newEntriesKeys }
                .plus(entries)
        }
    }

    suspend fun insert(entry: T): Unit = insert(listOf(entry))

    suspend fun insert(entries: List<T>): Unit = updateRaw { currentValue -> currentValue + entries }

    suspend fun count(): Int {
        return mutex.withLock {
            entries.value.size
        }
    }

    suspend fun updateRaw(updater: suspend (List<T>) -> List<T>) {
        mutex.withLock {
            val currentValue = entries.value
            val newValue = updater(currentValue).toList()
            if (currentValue != newValue) {
                entries.value = newValue
                onUpdate()
            }
        }
    }

    suspend fun updateBy(predicate: (T) -> Boolean, updater: (T) -> T) {
        updateRaw { currentValue ->
            currentValue.map { entry ->
                if (predicate(entry)) {
                    updater(entry)
                } else {
                    entry
                }
            }
        }
    }

    suspend fun firstOrNull(predicate: (T) -> Boolean): T? {
        return mutex.withLock {
            entries.value.firstOrNull(predicate)
        }
    }

    suspend fun getAll(): List<T> {
        return mutex.withLock {
            entries.value.toList()
        }
    }

    suspend fun filter(predicate: (T) -> Boolean): List<T> {
        return mutex.withLock {
            entries.value.filter(predicate)
        }
    }

    fun observe(): Flow<List<T>> = entries.asStateFlow()

    fun observeBy(predicate: (T) -> Boolean): Flow<List<T>> {
        return entries
            .map { list -> list.filter(predicate) }
            .distinctUntilChanged()
    }

    suspend fun deleteBy(predicate: (T) -> Boolean) {
        updateRaw { currentValue -> currentValue.filterNot(predicate) }
    }

    suspend fun deleteAll() {
        updateRaw { emptyList() }
    }
}
