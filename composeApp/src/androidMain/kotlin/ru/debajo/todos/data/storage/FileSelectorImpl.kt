package ru.debajo.todos.data.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import io.github.aakira.napier.Napier
import ru.debajo.todos.ActivityResultLaunchers
import ru.debajo.todos.common.canRead
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileSelectorImpl(
    private val activityResultLaunchersProvider: () -> ActivityResultLaunchers,
    private val contentResolver: ContentResolver,
) : FileSelector {

    private val activityResultLaunchers: ActivityResultLaunchers
        get() = activityResultLaunchersProvider()

    override suspend fun create(name: String, extension: String): StorageFile? {
        val uri = activityResultLaunchers.createDocument("$name.$extension") ?: return null
        val storageFile = uri.toStorageFile() ?: return null
        if (storageFile.extension != extension) {
            return null
        }
        if (!contentResolver.canRead(uri)) {
            return null
        }
        return storageFile
    }

    override suspend fun select(): StorageFile? {
        val uri = activityResultLaunchers.selectDocument() ?: return null
        val storageFile = uri.toStorageFile() ?: return null
        if (!contentResolver.canRead(uri)) {
            return null
        }
        return storageFile
    }

    private fun Uri.toStorageFile(): StorageFile? {
        return runCatching { toStorageFileUnsafe() }
            .onFailure { Napier.e("toStorageFile error", it) }
            .getOrNull()
    }

    private fun Uri.toStorageFileUnsafe(): StorageFile? {
        val absolutePath = path ?: return null
        val fileName = getFileName(this) ?: return null
        val extensionDelimiterIndex = fileName.indexOfLast { it == '.' }
        if (extensionDelimiterIndex == -1) {
            return null
        }
        val name = fileName.substring(0, extensionDelimiterIndex)
        val extension = fileName.substring(extensionDelimiterIndex)
        return StorageFile(
            absolutePath = absolutePath,
            name = name,
            extension = extension,
        )
    }

    private fun getFileName(uri: Uri): String? {
        return runCatching { getFileNameUnsafe(uri) }
            .onFailure { Napier.e("getFileName error", it) }
            .getOrNull()
    }

    private fun getFileNameUnsafe(uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }
}
