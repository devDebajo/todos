package ru.debajo.todos.data.storage.codec

import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta

interface StorageFileDecoder {
    suspend fun isEncrypted(file: StorageFile): Boolean

    suspend fun getTimestamp(file: StorageFile, pinHash: PinHash?): Long

    suspend fun decode(file: StorageFile, pinHash: PinHash?): StorageSnapshotWithMeta
}
