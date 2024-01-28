package ru.debajo.todos.data.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override fun observeChanged(files: List<StorageFile>): Flow<StorageFile> {
        val currentState = HashMap<String, Long>()
        for (file in files) {
            currentState[file.absolutePath] = File(file.absolutePath).lastModified()
        }

        return flow {
            while (true) {
                delay(2000)
                for (file in files) {
                    val lastModified = file.lastModified
                    if (currentState[file.absolutePath] != lastModified) {
                        currentState[file.absolutePath] = lastModified
                        emit(file)
                    }
                }
            }
        }
    }

    private val StorageFile.lastModified: Long
        get() = File(absolutePath).lastModified()
}

private class FileWriterImpl(private val outputStream: OutputStream) : FileWriter {
    override suspend fun write(content: String): Unit = outputStream.write(content)
}

private class FileReaderImpl(private val inputStream: InputStream) : FileReader {
    override suspend fun content(): String = inputStream.content()
}
