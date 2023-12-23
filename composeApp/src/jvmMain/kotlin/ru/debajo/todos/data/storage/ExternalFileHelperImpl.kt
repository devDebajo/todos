package ru.debajo.todos.data.storage

import com.russhwolf.settings.Settings
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
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

class ExternalFileHelperImpl(
    private val settings: Settings,
    private val appScope: CoroutineScope,
) : ExternalFileHelper {

    private val _fileUri: MutableStateFlow<String?> = MutableStateFlow(null)
    override val fileUri: StateFlow<String?> = _fileUri.asStateFlow()

    init {
        appScope.launch {
            _fileUri.value = loadUri()
        }
    }

    override suspend fun openOutputStream(): OutputStream {
        val uri = _fileUri.filterNotNull().first()
        return File(uri).outputStream()
    }

    override suspend fun openInputStream(): InputStream {
        val uri = _fileUri.filterNotNull().first()
        return File(uri).inputStream()
    }

    override fun create() {
        object : FileDialog(null as Frame?, "Create a file", SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    val uri = "$directory$file"
                    val file = File(uri)
                    runCatching { file.delete() }
                    runCatching { file.createNewFile() }
                    appScope.launch { offer(uri) }
                }
            }
        }.also {
            it.file = "todos.tds"
            it.isVisible = true
        }
    }

    override fun selectFile() {
        object : FileDialog(null as Frame?, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    appScope.launch { offer("$directory$file") }
                }
            }
        }.isVisible = true
    }

    override suspend fun offer(uri: String): Boolean {
        return withContext(Dispatchers.IO) {
            if (File(uri).exists()) {
                _fileUri.value = uri
                settings.putString(FILE_URI_KEY, uri)
                true
            } else {
                false
            }
        }
    }

    private suspend fun loadUri(): String? {
        return withContext(Dispatchers.IO) {
            runCatching { loadUriBlocking() }.getOrNull()
        }
    }

    private fun loadUriBlocking(): String? {
        val uri = settings.getString(FILE_URI_KEY, "")
        if (uri.isEmpty()) {
            return null
        }

        val file = File(uri)
        return if (file.exists()) {
            uri
        } else {
            null
        }
    }

    private companion object {
        const val FILE_URI_KEY = "FILE_URI_KEY"
    }
}
