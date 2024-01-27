package ru.debajo.todos.data.storage

import io.github.aakira.napier.Napier
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.SecuredPreferences

internal class DatabaseSnapshotSaver(
    private val databaseSnapshotHelper: DatabaseSnapshotHelper,
    private val storageFilesList: StorageFilesList,
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
    private val fileCodecHelper: FileCodecHelper,
    private val securedPreferences: SecuredPreferences,
) {

    private val mutex: Mutex = Mutex()
    private val onUpdateMutex: Mutex = Mutex()

    suspend fun save() {
        mutex.locked {
            val file = storageFilesList.currentFile
            if (file != null) {
                saveFile(file)
            }
        }
    }

    suspend fun saveEmpty(file: StorageFile, pinHash: PinHash?) {
        val snapshot = StorageSnapshotWithMeta(
            snapshot = StorageSnapshot(),
            absolutePath = file.absolutePath,
            editTimestampUtc = Clock.System.now().toEpochMilliseconds(),
            encrypted = pinHash != null,
        )
        val stream = fileHelper.openFileWriter(file)
        val fileContent = fileCodecHelper.encode(snapshot, file, pinHash)
        stream.write(fileContent)
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
            fileHelper.openFileWriter(file).write(content)

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
        val encrypted = fileCodecHelper.isEncrypted(file)
        if (encrypted && pinHash == null) {
            return
        }

        val snapshot = databaseSnapshotHelper.getSnapshot(needToSave.timestamp, encrypted)
        if (snapshot.absolutePath != file.absolutePath) {
            return
        }

        val stream = fileHelper.openFileWriter(file)
        val fileContent = fileCodecHelper.encode(snapshot, file, pinHash)
        stream.write(fileContent)
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

    suspend fun onUpdate() {
        onUpdateMutex.locked {
            val now = Clock.System.now().toEpochMilliseconds()
            val file = storageFilesList.awaitCurrentFile()
            securedPreferences.putLong(getKey(file), now)
        }
    }

    private suspend fun loadUnsafe(): StorageSnapshotWithMeta {
        val file = storageFilesList.awaitCurrentFile()
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

    private suspend fun <T> Mutex.locked(context: CoroutineContext = Dispatchers.IO, action: suspend () -> T): T {
        return withLock { withContext(context) { action() } }
    }

    private companion object {
        const val LAST_UPDATE_KEY: String = "last_update"
    }
}
