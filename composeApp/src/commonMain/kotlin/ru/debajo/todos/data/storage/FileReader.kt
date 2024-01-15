package ru.debajo.todos.data.storage

import kotlinx.coroutines.flow.Flow

interface FileReader {
    fun lineFlow(): Flow<String>
}
