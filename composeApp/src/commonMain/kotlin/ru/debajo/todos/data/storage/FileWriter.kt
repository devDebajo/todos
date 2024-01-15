package ru.debajo.todos.data.storage

interface FileWriter {
    suspend fun write(content: String)
}
