package ru.debajo.todos.data.storage

interface FileReader {
    suspend fun content(): String
}

class ConstantReader(val value: String) : FileReader {
    override suspend fun content(): String = value
}
