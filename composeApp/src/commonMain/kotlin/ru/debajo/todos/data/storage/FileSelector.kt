package ru.debajo.todos.data.storage

import ru.debajo.todos.data.storage.model.StorageFile

interface FileSelector {
    suspend fun create(name: String, extension: String): StorageFile?

    suspend fun select(): StorageFile?
}

