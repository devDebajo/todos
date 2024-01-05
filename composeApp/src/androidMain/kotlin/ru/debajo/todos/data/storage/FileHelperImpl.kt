package ru.debajo.todos.data.storage

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import io.github.aakira.napier.Napier
import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.common.canRead
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl(
    private val contentResolver: ContentResolver,
) : FileHelper {

    override fun createStorageFile(path: String): StorageFile? = Uri.parse(path).toStorageFile()

    override fun canRead(file: StorageFile): Boolean {
        val uri = Uri.parse(file.absolutePath)
        return if (contentResolver.canRead(uri)) {
            uri.requestPersistablePermission()
            true
        } else {
            false
        }
    }

    override suspend fun openOutputStream(file: StorageFile): OutputStream {
        return contentResolver.openOutputStream(Uri.parse(file.absolutePath), "wt")!!
    }

    override suspend fun openInputStream(file: StorageFile): InputStream {
        return contentResolver.openInputStream(Uri.parse(file.absolutePath))!!
    }

    private fun Uri.requestPersistablePermission() {
        if (contentResolver.persistedUriPermissions.any { it.uri == this && it.isWritePermission && it.isReadPermission }) {
            return
        }

        runCatching {
            contentResolver.takePersistableUriPermission(
                this,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun Uri.toStorageFile(): StorageFile? {
        return runCatching { toStorageFileUnsafe() }
            .onFailure { Napier.e("toStorageFile error", it) }
            .getOrNull()
    }

    private fun Uri.toStorageFileUnsafe(): StorageFile? {
        val absolutePath = toString()
        val fileName = getFileName(this) ?: return null
        val extensionDelimiterIndex = fileName.indexOfLast { it == '.' }
        if (extensionDelimiterIndex == -1) {
            return null
        }
        val name = fileName.substring(0, extensionDelimiterIndex)
        val extension = fileName.substring(extensionDelimiterIndex + 1)
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
