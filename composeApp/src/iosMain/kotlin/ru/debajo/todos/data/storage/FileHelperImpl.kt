package ru.debajo.todos.data.storage

import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl : FileHelper {
    override fun createStorageFile(path: String): StorageFile? {
        TODO("Not yet implemented")
    }

    override fun canRead(file: StorageFile): Boolean {
        TODO("Not yet implemented")
    }

    override fun openOutputStream(file: StorageFile): FileWriter {
        TODO("Not yet implemented")
    }

    override fun openInputStream(file: StorageFile): FileReader {
        TODO("Not yet implemented")
    }
}
