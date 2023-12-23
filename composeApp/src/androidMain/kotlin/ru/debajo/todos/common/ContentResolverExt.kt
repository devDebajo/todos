package ru.debajo.todos.common

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.InputStream
import java.io.OutputStream

@WorkerThread
internal fun ContentResolver.canRead(uri: Uri): Boolean {
    var stream: InputStream? = null
    try {
        stream = openInputStream(uri)
    } catch (e: Throwable) {
        return false
    } finally {
        stream?.close()
    }
    return true
}

@WorkerThread
internal fun ContentResolver.canWrite(uri: Uri): Boolean {
    var stream: OutputStream? = null
    try {
        stream = openOutputStream(uri)
    } catch (e: Throwable) {
        return false
    } finally {
        stream?.close()
    }
    return true
}
