package ru.debajo.todos.data.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.java.utils.content
import ru.debajo.todos.java.utils.write

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

    override fun openFileWriter(file: StorageFile): FileWriter {
        return FileWriterImpl(File(file.absolutePath).outputStream())
    }

    override fun openFileReader(file: StorageFile): FileReader {
        return FileReaderImpl(File(file.absolutePath).inputStream())
    }
}

private class FileWriterImpl(private val outputStream: OutputStream) : FileWriter {
    override suspend fun write(content: String): Unit = outputStream.write(content)
}

private class FileReaderImpl(private val inputStream: InputStream) : FileReader {
    override suspend fun content(): String = inputStream.content()
}
