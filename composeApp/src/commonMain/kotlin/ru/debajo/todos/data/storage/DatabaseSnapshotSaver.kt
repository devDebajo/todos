package ru.debajo.todos.data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.model.StorageSnapshot

@OptIn(ExperimentalSerializationApi::class)
class DatabaseSnapshotSaver(
    private val json: Json,
    private val databaseSnapshotHelper: DatabaseSnapshotHelper,
    private val externalFileHelper: ExternalFileHelper,
) {
    private val mutex: Mutex = Mutex()
    private val _saving: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    suspend fun save() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                _saving.value = true
                try {
                    val stream = externalFileHelper.openOutputStream()
                    val snapshot = databaseSnapshotHelper.getSnapshot()
                    stream.use {
                        json.encodeToStream(StorageSnapshot.serializer(), snapshot, it)
                    }
                } finally {
                    _saving.value = false
                }
            }
        }
    }

    suspend fun load() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                val snapshot = runCatchingAsync { loadUnsafe() }
                    .onFailure { it.printStackTrace() }
                    .getOrElse { StorageSnapshot() }
                databaseSnapshotHelper.replace(snapshot)
            }
        }
    }

    private suspend fun loadUnsafe(): StorageSnapshot {
        val stream = externalFileHelper.openInputStream()
        return stream.use {
            json.decodeFromStream(StorageSnapshot.serializer(), it)
        }
    }
}
