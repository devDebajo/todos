package ru.debajo.todos.data.storage

import kotlinx.coroutines.flow.Flow
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl : FileHelper {
    override fun createStorageFile(path: String): StorageFile? {
        TODO("Not yet implemented")
    }

    override fun canRead(file: StorageFile): Boolean {
        TODO("Not yet implemented")
    }

    override fun openFileWriter(file: StorageFile): FileWriter {
        TODO("Not yet implemented")
    }

    override fun openFileReader(file: StorageFile): FileReader {
        TODO("Not yet implemented")
    }

    override fun observeChanged(files: List<StorageFile>): Flow<StorageFile> {
        TODO("Not yet implemented")
    }
}
