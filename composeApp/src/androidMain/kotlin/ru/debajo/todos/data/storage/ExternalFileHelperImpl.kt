package ru.debajo.todos.data.storage

import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.debajo.todos.ActivityResultLaunchers
import ru.debajo.todos.common.canRead

internal class ExternalFileHelperImpl(
    private val activityResultLaunchersProvider: () -> ActivityResultLaunchers,
    private val sharedPreferences: SharedPreferences,
    private val contentResolver: ContentResolver,
    private val appScope: CoroutineScope,
) : ExternalFileHelper {

    private val activityResultLaunchers: ActivityResultLaunchers
        get() = activityResultLaunchersProvider()

    private val _fileUri: MutableStateFlow<String?> = MutableStateFlow(null)
    override val fileUri: StateFlow<String?> = _fileUri.asStateFlow()

    init {
        appScope.launch {
            _fileUri.value = loadUri()?.toString()
        }
    }

    override suspend fun openOutputStream(): OutputStream {
        val uri = fileUri.filterNotNull().first()
        return contentResolver.openOutputStream(Uri.parse(uri), "w")!!
    }

    override suspend fun openInputStream(): InputStream {
        val uri = fileUri.filterNotNull().first()
        return contentResolver.openInputStream(Uri.parse(uri))!!
    }

    override fun create() {
        activityResultLaunchers.createDocument("todos.tds") { uri ->
            appScope.launch {
                if (offer(uri.toString())) {
                    uri.requestPersistablePermission()
                }
            }
        }
    }

    override fun selectFile() {
        activityResultLaunchers.selectDocument { uri ->
            appScope.launch {
                if (offer(uri.toString())) {
                    uri.requestPersistablePermission()
                }
            }
        }
    }

    override suspend fun offer(uri: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (contentResolver.canRead(Uri.parse(uri))) {
                _fileUri.value = uri
                sharedPreferences.edit().putString(FILE_URI_KEY, uri).apply()
                true
            } else {
                false
            }
        }
    }

    private suspend fun loadUri(): Uri? {
        return withContext(Dispatchers.IO) {
            runCatching { loadUriBlocking() }.getOrNull()
        }
    }

    @WorkerThread
    private fun loadUriBlocking(): Uri? {
        val uri = sharedPreferences.getString(FILE_URI_KEY, "")
        if (uri.isNullOrEmpty()) {
            return null
        }

        val parsedUri = Uri.parse(uri)
        return if (contentResolver.canRead(parsedUri)) {
            parsedUri
        } else {
            null
        }
    }

    private fun Uri.requestPersistablePermission() {
        runCatching {
            contentResolver.takePersistableUriPermission(
                this,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private companion object {
        const val FILE_URI_KEY = "FILE_URI_KEY"
    }
}
