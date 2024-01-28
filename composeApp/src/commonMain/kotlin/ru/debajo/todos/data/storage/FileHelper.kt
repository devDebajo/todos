package ru.debajo.todos.data.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import ru.debajo.todos.common.syncMutableMap
import ru.debajo.todos.data.storage.model.StorageFile

interface FileHelper {
    fun createStorageFile(path: String): StorageFile?

    fun canRead(file: StorageFile): Boolean

    fun openFileWriter(file: StorageFile): FileWriter

    fun openFileReader(file: StorageFile): FileReader

    fun observeChanged(files: List<StorageFile>): Flow<StorageFile>
}

internal expect fun createFileHelper(): FileHelper

internal class FileHelperContentCache(private val delegate: FileHelper) : FileHelper by delegate {

    private val cache: MutableMap<String, String> = syncMutableMap()

    override fun openFileWriter(file: StorageFile): FileWriter {
        val writer = delegate.openFileWriter(file)
        return object : FileWriter {
            override suspend fun write(content: String) {
                writer.write(content)
                cache[file.absolutePath] = content
            }
        }
    }

    override fun openFileReader(file: StorageFile): FileReader {
        val content = cache[file.absolutePath]
        return if (content == null) {
            val reader = delegate.openFileReader(file)
            object : FileReader {
                override suspend fun content(): String {
                    val currentContent = reader.content()
                    cache[file.absolutePath] = currentContent
                    return currentContent
                }
            }
        } else {
            ConstantReader(content)
        }
    }

    // TODO это сломает работу с файлом в режиме открытого файла. Поэтому пока использовать можно только в FileConfigViewModel
    override fun observeChanged(files: List<StorageFile>): Flow<StorageFile> {
        return delegate.observeChanged(files).onEach { file ->
            cache.remove(file.absolutePath)
        }
    }
}
