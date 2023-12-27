package ru.debajo.todos.data.storage

import com.russhwolf.settings.Settings
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageTimestampSnapshot
import ru.debajo.todos.ui.AppLifecycle
import ru.debajo.todos.ui.awaitState

@OptIn(ExperimentalSerializationApi::class)
class DatabaseSnapshotSaver(
    private val json: Json,
    private val databaseSnapshotHelper: DatabaseSnapshotHelper,
    private val externalFileHelper: ExternalFileHelper,
    private val settings: Settings,
    private val appLifecycle: AppLifecycle,
) : DatabaseChangeListener {

    private val mutex: Mutex = Mutex()
    private val onUpdateMutex: Mutex = Mutex()

    suspend fun save() {
        if (mutex.isLocked) {
            return
        }

        mutex.locked {
            appLifecycle.awaitState(AppLifecycle.State.Resumed)
            val needToSave = needToSave()
            if (needToSave is NeedToSave.Yes) {
                val stream = externalFileHelper.openOutputStream()
                val snapshot = databaseSnapshotHelper.getSnapshot(needToSave.timestamp)
                stream.use {
                    json.encodeToStream(StorageSnapshot.serializer(), snapshot, it)
                }
            }
        }
    }

    suspend fun load() {
        mutex.locked {
            val snapshot = runCatchingAsync { loadUnsafe() }
                .onFailure { it.printStackTrace() }
                .getOrElse { StorageSnapshot() }
            databaseSnapshotHelper.replace(snapshot)
        }
    }

    private suspend fun loadUnsafe(): StorageSnapshot {
        val stream = externalFileHelper.openInputStream()
        return stream.use {
            json.decodeFromStream(StorageSnapshot.serializer(), it)
        }
    }

    private suspend fun loadTimestampFromFile(): Instant? {
        return runCatchingAsync { loadTimestampUnsafe() }.getOrNull()
    }

    private suspend fun loadTimestampUnsafe(): Instant {
        val stream = externalFileHelper.openInputStream()
        val timestamp = stream.use {
            json.decodeFromStream(StorageTimestampSnapshot.serializer(), it)
        }.timestamp
        return Instant.fromEpochMilliseconds(timestamp)
    }

    override suspend fun onUpdate() {
        onUpdateMutex.locked {
            val now = Clock.System.now().toEpochMilliseconds()
            settings.putLong(getKey(), now)
        }
    }

    private suspend fun needToSave(): NeedToSave {
        val key = getKey()
        val timestampFromSettings = onUpdateMutex.locked {
            settings.getLong(key, -1)
        }
        if (timestampFromSettings == -1L) {
            return NeedToSave.No
        }
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

    private suspend fun getKey(): String {
        val uri = externalFileHelper.awaitUri()
        return "${LAST_UPDATE_KEY}_${uri}"
    }

    private suspend fun <T> Mutex.locked(context: CoroutineContext = Dispatchers.IO, action: suspend () -> T): T {
        return withLock { withContext(context) { action() } }
    }

    private companion object {
        const val LAST_UPDATE_KEY: String = "last_update"
    }
}
