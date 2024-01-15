package ru.debajo.todos.data.storage

import ru.debajo.todos.data.storage.model.StorageFile

interface FileHelper {
    fun createStorageFile(path: String): StorageFile?

    fun canRead(file: StorageFile): Boolean

    fun openOutputStream(file: StorageFile): FileWriter

    fun openInputStream(file: StorageFile): FileReader
}
