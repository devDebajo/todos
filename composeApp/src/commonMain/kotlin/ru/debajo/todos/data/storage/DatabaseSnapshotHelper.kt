package ru.debajo.todos.data.storage

import ru.debajo.todos.data.storage.model.StorageSnapshot

class DatabaseSnapshotHelper {
    suspend fun getSnapshot(): StorageSnapshot {
        // TODO implement
        return StorageSnapshot()
    }

    suspend fun replace(snapshot: StorageSnapshot) {
        // TODO implement
    }
}
