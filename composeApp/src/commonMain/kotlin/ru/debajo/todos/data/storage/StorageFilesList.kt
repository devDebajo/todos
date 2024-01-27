package ru.debajo.todos.data.storage

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.security.SecuredPreferences

class StorageFilesList(
    private val preferences: Preferences,
    private val securedPreferences: SecuredPreferences,
    appScope: CoroutineScope,
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
    private val fileCodecHelper: FileCodecHelper,
    private val fileSession: FileSession,
) {
    private val _files: MutableStateFlow<List<StorageFile>?> = MutableStateFlow(null)
    val files: StateFlow<List<StorageFile>?> = _files.asStateFlow()

    val currentFile: StorageFile?
        get() = fileSession.currentFile

    init {
        appScope.launch {
            _files.value = loadFilesList()
        }
    }

    suspend fun awaitCurrentFile(): StorageFile = fileSession.awaitOpened()

    suspend fun selectFileFromList(file: StorageFile): Boolean {
        if (file !in _files.value.orEmpty()) {
            return false
        }

        if (!fileHelper.canRead(file)) {
            return false
        }

        fileSession.open(file)
        saveLastFile(file)
        return true
    }

    suspend fun isSelectLastFile(): Boolean {
        return preferences.getBoolean(SelectLastFileKey) == true
    }

    suspend fun setSelectLastFile(value: Boolean) {
        preferences.putBoolean(SelectLastFileKey, value)
    }

    suspend fun tryAddFile(path: String): Boolean {
        val file = fileHelper.createStorageFile(path) ?: return false
        return tryAddFile(file, null)
    }

    suspend fun tryAddFile(file: StorageFile, pinHash: PinHash?): Boolean {
        if (fileSession.currentFile?.absolutePath == file.absolutePath) {
            return false
        }

        return withContext(Dispatchers.IO) {
            if (fileHelper.canRead(file)) {
                addFileToList(file)
                if (fileCodecHelper.isEncrypted(file) && pinHash != null) {
                    filePinStorage.save(file, pinHash)
                }
                true
            } else {
                false
            }
        }
    }

    suspend fun savePinHash(file: StorageFile, pinHash: PinHash) {
        filePinStorage.save(file, pinHash)
    }

    suspend fun loadLastFile(): StorageFile? {
        val path = securedPreferences.getString(LastSelectedFileKey) ?: return null
        return fileHelper.createStorageFile(path)
    }

    suspend fun deleteFileFromList(file: StorageFile) {
        val lastFile = loadLastFile()
        if (lastFile?.absolutePath == file.absolutePath) {
            securedPreferences.remove(LastSelectedFileKey)
        }

        val newList = _files.value.orEmpty().filter { it.absolutePath != file.absolutePath }
        _files.value = newList
        saveFilesListUnsafe(newList)
    }

    private suspend fun addFileToList(file: StorageFile) {
        val newList = (_files.value.orEmpty() + listOf(file)).distinctBy { it.absolutePath }
        _files.value = newList
        saveFilesListUnsafe(newList)
    }

    private suspend fun loadFilesList(): List<StorageFile> {
        return runCatchingAsync { loadFilesListUnsafe() }
            .onFailure { Napier.e("loadFilesList error", it) }
            .getOrElse { emptyList() }
    }

    private suspend fun saveFilesListUnsafe(files: List<StorageFile>) {
        securedPreferences.putStringList(FilesListKey, files.map { it.absolutePath })
    }

    private suspend fun loadFilesListUnsafe(): List<StorageFile> {
        return securedPreferences.getStringList(FilesListKey).orEmpty()
            .mapNotNull { uri -> fileHelper.createStorageFile(uri) }
    }

    private suspend fun saveLastFile(file: StorageFile) {
        securedPreferences.putString(LastSelectedFileKey, file.absolutePath)
    }

    private companion object {
        const val FilesListKey: String = "FilesList"
        const val SelectLastFileKey: String = "SelectLastFile"
        const val LastSelectedFileKey: String = "LastSelectedFile"
    }
}
