package ru.debajo.todos.data.storage.model

data class StorageSnapshotWithMeta(
    val snapshot: StorageSnapshot,
    val absolutePath: String,
    val editTimestampUtc: Long,
)
