package ru.debajo.todos.data.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl : FileHelper {
    override fun createStorageFile(path: String): StorageFile {
        val file = File(path)
        return StorageFile(
            absolutePath = file.absolutePath,
            name = file.nameWithoutExtension,
            extension = file.extension
        )
    }

    override fun canRead(file: StorageFile): Boolean {
        return File(file.absolutePath).exists()
    }

    override fun openOutputStream(file: StorageFile): OutputStream {
        return File(file.absolutePath).outputStream()
    }

    override fun openInputStream(file: StorageFile): InputStream {
        return File(file.absolutePath).inputStream()
    }
}
