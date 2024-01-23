package ru.debajo.todos.data.storage

import android.content.ContentResolver
import ru.debajo.todos.app.ActivityResultLaunchers
import ru.debajo.todos.common.canRead
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileSelectorImpl(
    private val activityResultLaunchersProvider: () -> ActivityResultLaunchers,
    private val contentResolver: ContentResolver,
    private val fileHelper: FileHelper,
) : FileSelector {

    private val activityResultLaunchers: ActivityResultLaunchers
        get() = activityResultLaunchersProvider()

    override suspend fun create(nameWithExtension: String): StorageFile? {
        val uri = activityResultLaunchers.createDocument(nameWithExtension) ?: return null
        val storageFile = fileHelper.createStorageFile(uri.toString()) ?: return null
        if (!contentResolver.canRead(uri)) {
            return null
        }
        return storageFile
    }

    override suspend fun select(): StorageFile? {
        val uri = activityResultLaunchers.selectDocument() ?: return null
        val storageFile = fileHelper.createStorageFile(uri.toString()) ?: return null
        if (!contentResolver.canRead(uri)) {
            return null
        }
        return storageFile
    }
}
