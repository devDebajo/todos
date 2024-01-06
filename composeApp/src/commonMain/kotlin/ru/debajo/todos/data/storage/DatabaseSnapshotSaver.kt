package ru.debajo.todos.data.storage

import io.github.aakira.napier.Napier
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageTimestampSnapshot
import ru.debajo.todos.security.AesHelper
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.SecuredPreferences

class DatabaseSnapshotSaver(
    private val json: Json,
    private val databaseSnapshotHelper: DatabaseSnapshotHelper,
    private val storageFileManager: StorageFileManager,
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
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

    suspend fun saveFile(file: StorageFile) {
        val needToSave = needToSave(file)
        if (needToSave !is NeedToSave.Yes) {
            return
        }

        val pinHash = filePinStorage.get(file)
        if (file.encrypted && pinHash == null) {
            return
        }

        val snapshot = databaseSnapshotHelper.getSnapshot(needToSave.timestamp)
        if (snapshot.absolutePath != file.absolutePath) {
            return
        }

        val stream = fileHelper.openOutputStream(file)
        var fileContent = json.encodeToString(StorageSnapshot.serializer(), snapshot)
        if (file.encrypted) {
            fileContent = AesHelper.encrypt(pinHash!!.pinHash, fileContent)
        }
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

    private suspend fun loadUnsafe(): StorageSnapshot {
        val file = storageFileManager.awaitCurrentFile()
        val fileContent = loadFileContentUnsafe(file)
        return json.decodeFromString(StorageSnapshot.serializer(), fileContent).copy(
            absolutePath = file.absolutePath
        )
    }

    private suspend fun loadTimestampFromFile(): Instant? {
        return runCatchingAsync { loadTimestampUnsafe() }.getOrNull()
    }

    private suspend fun loadTimestampUnsafe(): Instant {
        val fileContent = loadFileContentUnsafe()
        val timestamp = json.decodeFromString(StorageTimestampSnapshot.serializer(), fileContent).timestamp
        return Instant.fromEpochMilliseconds(timestamp)
    }

    private suspend fun loadFileContentUnsafe(): String {
        val file = storageFileManager.awaitCurrentFile()
        return loadFileContentUnsafe(file)
    }

    private suspend fun loadFileContentUnsafe(file: StorageFile): String {
        val pinHash = filePinStorage.get(file)
        var fileContent = fileHelper.openInputStream(file).bufferedReader().readText()
        if (file.encrypted) {
            fileContent = AesHelper.decrypt(pinHash!!.pinHash, fileContent)
        }
        return fileContent
    }

    private suspend fun needToSave(file: StorageFile): NeedToSave {
        val key = getKey(file)
        val timestampFromSettings = onUpdateMutex.locked { securedPreferences.getLong(key) } ?: return NeedToSave.No
        val timestampFromFile = loadTimestampFromFile() ?: return NeedToSave.Yes(Clock.System.now())
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
