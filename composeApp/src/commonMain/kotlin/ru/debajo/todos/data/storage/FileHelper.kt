package ru.debajo.todos.data.storage

import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.data.storage.model.StorageFile

interface FileHelper {
    fun createStorageFile(path: String): StorageFile?

    fun canRead(file: StorageFile): Boolean

    suspend fun openOutputStream(file: StorageFile): OutputStream

    suspend fun openInputStream(file: StorageFile): InputStream
}
