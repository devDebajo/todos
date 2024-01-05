package ru.debajo.todos.data.storage

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import java.io.InputStream
import java.io.OutputStream
import ru.debajo.todos.common.canRead
import ru.debajo.todos.data.storage.model.StorageFile

internal class FileHelperImpl(
    private val contentResolver: ContentResolver,
) : FileHelper {

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
}
