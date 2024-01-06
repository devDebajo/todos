package ru.debajo.todos.data.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.model.StorageFile

@Deprecated("Use StorageFileManager")
class ExternalFileHelper(
    private val preferences: Preferences,
    private val appScope: CoroutineScope,
    private val fileSelector: FileSelector,
    private val fileHelper: FileHelper,
) {

    private val _fileUri: MutableStateFlow<StorageFile?> = MutableStateFlow(null)
    val fileUri: StateFlow<StorageFile?> = _fileUri.asStateFlow()

    init {
        appScope.launch {
            _fileUri.value = loadUri()
        }
    }

    fun create() {
        appScope.launch {
            val file = fileSelector.create("todos", "tds")
            if (file != null) {
                offer(file.absolutePath)
            }
        }
    }

    fun selectFile() {
        appScope.launch {
            val file = fileSelector.select()
            if (file != null) {
                offer(file.absolutePath)
            }
        }
    }

    private suspend fun offer(uri: String): Boolean {
        return withContext(Dispatchers.IO) {
            val file = fileHelper.createStorageFile(uri)
            if (file != null && fileHelper.canRead(file)) {
                _fileUri.value = file
                preferences.putString(FILE_URI_KEY, uri)
                true
            } else {
                false
            }
        }
    }

    private suspend fun loadUri(): StorageFile? {
        return withContext(Dispatchers.IO) {
            runCatchingAsync { loadUriUnsafe() }.getOrNull()
        }
    }

    private suspend fun loadUriUnsafe(): StorageFile? {
        val uri = preferences.getString(FILE_URI_KEY)
        if (uri.isNullOrEmpty()) {
            return null
        }
        val storageFile = fileHelper.createStorageFile(uri) ?: return null
        return if (fileHelper.canRead(storageFile)) {
            storageFile
        } else {
            null
        }
    }

    private companion object {
        const val FILE_URI_KEY = "FILE_URI_KEY"
    }
}

suspend fun ExternalFileHelper.awaitUri(): String {
    return fileUri.filterNotNull().first().absolutePath
}
