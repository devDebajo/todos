package ru.debajo.todos.data.storage.model

import ru.debajo.todos.security.EncryptionUnit

data class StorageSnapshotWithMeta(
    val snapshot: StorageSnapshot,
    val absolutePath: String,
    val editTimestampUtc: Long,
    val encryptionUnit: EncryptionUnit?,
) {
    val encrypted: Boolean = encryptionUnit != null
}
