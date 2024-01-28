package ru.debajo.todos.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import ru.debajo.todos.common.syncMutableMap
import ru.debajo.todos.data.storage.model.StorageFile

interface FileHelper {
    fun createStorageFile(path: String): StorageFile?

    fun canRead(file: StorageFile): Boolean

    fun openFileWriter(file: StorageFile): FileWriter

    fun openFileReader(file: StorageFile): FileReader

    fun getLastModified(file: StorageFile): Instant?
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

    fun clearCache(file: StorageFile) {
        cache.remove(file.absolutePath)
    }
}

// TODO это сломает работу с файлом в режиме открытого файла. Поэтому пока использовать можно только в FileConfigViewModel
private fun FileHelper.invalidateCacheIfCan(file: StorageFile) {
    if (this is FileHelperContentCache) {
        clearCache(file)
    }
}

fun FileHelper.observeChanged(files: List<StorageFile>): Flow<StorageFile> {
    val currentState = HashMap<String, Instant>()
    for (file in files) {
        val lastModified = getLastModified(file)
        if (lastModified != null) {
            currentState[file.absolutePath] = lastModified
        }
    }

    return flow {
        while (true) {
            delay(2000)
            for (file in files) {
                val lastModified = getLastModified(file) ?: continue
                if (currentState[file.absolutePath] != lastModified) {
                    currentState[file.absolutePath] = lastModified
                    this@observeChanged.invalidateCacheIfCan(file)
                    emit(file)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
