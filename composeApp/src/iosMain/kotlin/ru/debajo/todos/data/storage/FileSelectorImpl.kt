package ru.debajo.todos.data.storage

import ru.debajo.todos.data.storage.model.StorageFile

internal class FileSelectorImpl : FileSelector {
    override suspend fun create(nameWithExtension: String): StorageFile? {
        TODO("Not yet implemented")
    }

    override suspend fun select(): StorageFile? {
        TODO("Not yet implemented")
    }
}
