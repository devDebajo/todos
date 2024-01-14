package ru.debajo.todos.data.storage.model

data class StorageSnapshotWithMeta(
    val snapshot: StorageSnapshot = StorageSnapshot(),
    val absolutePath: String,
    val editTimestampUtc: Long = 0,
    val encrypted: Boolean = false,
)
