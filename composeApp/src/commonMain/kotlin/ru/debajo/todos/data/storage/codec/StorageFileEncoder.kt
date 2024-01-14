package ru.debajo.todos.data.storage.codec

import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta

interface StorageFileEncoder {
    suspend fun encode(snapshot: StorageSnapshotWithMeta, file: StorageFile, pinHash: PinHash?): StorageFileExternalContent
}
