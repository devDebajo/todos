package ru.debajo.todos.data.storage

import io.github.aakira.napier.Napier
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
import ru.debajo.todos.security.SecuredPreferences

class StorageFileManager(
    private val preferences: Preferences,
    private val securedPreferences: SecuredPreferences,
    appScope: CoroutineScope,
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
) {
    private val _files: MutableStateFlow<List<StorageFile>> = MutableStateFlow(emptyList())
    val files: StateFlow<List<StorageFile>> = _files.asStateFlow()
    private val _currentFile: MutableStateFlow<StorageFile?> = MutableStateFlow(null)

    val currentFile: StorageFile?
        get() = _currentFile.value

    init {
        appScope.launch {
            _files.value = loadFilesList()
            if (isSelectLastFile()) {
                val lastFile = loadLastFile()
                if (lastFile != null) {
                    selectFile(lastFile)
                }
            }
        }
    }

    fun isFileAlreadySelected(): Boolean {
        return _currentFile.value != null
    }

    suspend fun awaitCurrentFile(): StorageFile {
        return _currentFile.filterNotNull().first()
    }

    suspend fun isFileReadyToRead(): Boolean {
        val file = _currentFile.value ?: return false
        if (!fileHelper.canRead(file)) {
            return false
        }
        if (!file.encrypted) {
            return true
        }
        return filePinStorage.get(file) != null
    }

    suspend fun selectFile(file: StorageFile): Boolean {
        if (!file.isValidExtension) {
            return false
        }

        if (file !in _files.value) {
            return false
        }

        if (!fileHelper.canRead(file)) {
            return false
        }

        _currentFile.value = file
        saveLastFile(file)
        return true
    }

    suspend fun isSelectLastFile(): Boolean {
        return preferences.getBoolean(SelectLastFileKey) == true
    }

    suspend fun setSelectLastFile(value: Boolean) {
        preferences.putBoolean(SelectLastFileKey, value)
    }

    suspend fun trySelectFile(path: String): Boolean {
        if (_currentFile.value?.absolutePath == path) {
            return false
        }

        return withContext(Dispatchers.IO) {
            val file = fileHelper.createStorageFile(path)
            if (file != null) {
                selectFile(file)
            } else {
                false
            }
        }
    }

    private suspend fun loadFilesList(): List<StorageFile> {
        return runCatchingAsync { loadFilesListUnsafe() }
            .onFailure { Napier.e("loadFilesList error", it) }
            .getOrElse { emptyList() }
    }

    private suspend fun loadFilesListUnsafe(): List<StorageFile> {
        return securedPreferences.getStringList(FilesListKey).orEmpty()
            .mapNotNull { uri -> fileHelper.createStorageFile(uri) }
    }

    private suspend fun saveLastFile(file: StorageFile) {
        securedPreferences.putString(LastSelectedFileKey, file.absolutePath)
    }

    private suspend fun loadLastFile(): StorageFile? {
        val path = securedPreferences.getString(LastSelectedFileKey) ?: return null
        return fileHelper.createStorageFile(path)
    }

    private companion object {
        const val FilesListKey: String = "FilesList"
        const val SelectLastFileKey: String = "SelectLastFile"
        const val LastSelectedFileKey: String = "LastSelectedFile"
    }
}
