package ru.debajo.todos.data.storage.model

data class StorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String,
)
