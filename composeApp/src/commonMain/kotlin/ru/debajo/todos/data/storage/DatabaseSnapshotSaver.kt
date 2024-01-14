package ru.debajo.todos.data.storage

import io.github.aakira.napier.Napier
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.SecuredPreferences

class DatabaseSnapshotSaver(
    private val databaseSnapshotHelper: DatabaseSnapshotHelper,
    private val storageFileManager: StorageFileManager,
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
    private val fileCodecHelper: FileCodecHelper,
    private val securedPreferences: SecuredPreferences,
    private val appLifecycle: AppLifecycle,
) : DatabaseChangeListener {

    private val mutex: Mutex = Mutex()
    private val onUpdateMutex: Mutex = Mutex()

    suspend fun save(ignorePaused: Boolean = false) {
        if (appLifecycle.isPaused && !ignorePaused) {
            return
        }

        mutex.locked {
            val file = storageFileManager.currentFile
            if (file != null) {
                saveFile(file)
            }
        }
    }

    suspend fun saveLastFileSafe() {
        runCatchingAsync {
            val lastFile = storageFileManager.loadLastFile() ?: return
            saveFile(lastFile)
        }
    }

    suspend fun changePin(file: StorageFile, oldPinHash: PinHash? = null, newPinHash: PinHash? = null) {
        mutex.locked {
            val encrypted = fileCodecHelper.isEncrypted(file)
            if (encrypted && oldPinHash == null) {
                error("Could not change pin without old pin")
            }

            val snapshot = fileCodecHelper.decode(file, oldPinHash)
                .copy(encrypted = newPinHash != null)

            val content = fileCodecHelper.encode(snapshot, file, newPinHash)
            fileHelper.openOutputStream(file).bufferedWriter().use { it.write(content) }

            if (newPinHash == null) {
                filePinStorage.remove(file)
            } else {
                filePinStorage.save(file, newPinHash)
            }
        }
    }

    private suspend fun saveFile(file: StorageFile) {
        val needToSave = needToSave(file)
        if (needToSave !is NeedToSave.Yes) {
            return
        }

        val pinHash = filePinStorage.get(file)
        if (fileCodecHelper.isEncrypted(file) && pinHash == null) {
            return
        }

        val snapshot = databaseSnapshotHelper.getSnapshot(needToSave.timestamp)
        if (snapshot.absolutePath != file.absolutePath) {
            return
        }

        val stream = fileHelper.openOutputStream(file)
        val fileContent = fileCodecHelper.encode(snapshot, file, pinHash)
        stream.bufferedWriter().use { it.write(fileContent) }
    }

    suspend fun load(): Boolean {
        return mutex.locked {
            val snapshot = runCatchingAsync { loadUnsafe() }
                .onFailure { Napier.e("load error", it) }
                .getOrNull()
            if (snapshot == null) {
                false
            } else {
                databaseSnapshotHelper.replace(snapshot)
                true
            }
        }
    }

    override suspend fun onUpdate() {
        onUpdateMutex.locked {
            val now = Clock.System.now().toEpochMilliseconds()
            val file = storageFileManager.awaitCurrentFile()
            securedPreferences.putLong(getKey(file), now)
        }
    }

    private suspend fun loadUnsafe(): StorageSnapshotWithMeta {
        val file = storageFileManager.awaitCurrentFile()
        return loadFileContentUnsafe(file)
    }

    private suspend fun loadTimestampFromFile(file: StorageFile): Instant? {
        return runCatchingAsync { loadTimestampUnsafe(file) }.getOrNull()
    }

    private suspend fun loadTimestampUnsafe(file: StorageFile): Instant {
        val pinHash = filePinStorage.get(file)
        val timestamp = fileCodecHelper.getTimestamp(file, pinHash)
        return Instant.fromEpochMilliseconds(timestamp)
    }

    private suspend fun loadFileContentUnsafe(file: StorageFile): StorageSnapshotWithMeta {
        val pinHash = filePinStorage.get(file)
        return fileCodecHelper.decode(file, pinHash)
    }

    private suspend fun needToSave(file: StorageFile): NeedToSave {
        val key = getKey(file)
        val timestampFromSettings = onUpdateMutex.locked { securedPreferences.getLong(key) } ?: return NeedToSave.No
        val timestampFromFile = loadTimestampFromFile(file) ?: return NeedToSave.Yes(Clock.System.now())
        return if (timestampFromSettings > timestampFromFile.toEpochMilliseconds()) {
            NeedToSave.Yes(Instant.fromEpochMilliseconds(timestampFromSettings))
        } else {
            NeedToSave.No
        }
    }

    private sealed interface NeedToSave {
        data object No : NeedToSave
        data class Yes(val timestamp: Instant) : NeedToSave
    }

    private fun getKey(file: StorageFile): String {
        val hash = HashUtils.getHash(file.absolutePath)
        return "${LAST_UPDATE_KEY}_${hash}"
    }

    private suspend fun <T> Mutex.locked(context: CoroutineContext = IO, action: suspend () -> T): T {
        return withLock { withContext(context) { action() } }
    }

    private companion object {
        const val LAST_UPDATE_KEY: String = "last_update"
    }
}
