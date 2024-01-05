package ru.debajo.todos.data.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl : FileHelper {
    override fun canRead(file: StorageFile): Boolean {
        return File(file.absolutePath).exists()
    }

    override suspend fun openOutputStream(file: StorageFile): OutputStream {
        return File(file.absolutePath).outputStream()
    }

    override suspend fun openInputStream(file: StorageFile): InputStream {
        return File(file.absolutePath).inputStream()
    }
}
