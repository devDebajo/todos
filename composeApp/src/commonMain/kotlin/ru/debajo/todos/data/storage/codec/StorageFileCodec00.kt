package ru.debajo.todos.data.storage.codec

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta
import ru.debajo.todos.data.storage.model.encrypted
import ru.debajo.todos.security.AesHelper
import ru.debajo.todos.security.decryptString

@Deprecated("Use TDS01")
class StorageFileCodec00(
    private val json: Json,
    private val tokensProvider: TokensProvider,
) : StorageFileCodec {

    override suspend fun isEncrypted(file: StorageFile): Boolean = file.encrypted

    override suspend fun getTimestamp(file: StorageFile, pinHash: PinHash?): Long {
        return withContext(Dispatchers.IO) {
            runCatchingAsync { getTimestampUnsafe(file, pinHash) }.getOrThrow()
        }
    }

    override suspend fun decode(file: StorageFile, pinHash: PinHash?): StorageSnapshotWithMeta {
        val tokens = tokensProvider(file).toList()
        if (tokens.size != 1) {
            error("Not TDS00")
        }
        val fileContent = tokens[0]
        if (fileContent.isEmpty()) {
            return StorageSnapshotWithMeta(absolutePath = file.absolutePath)
        }

        val decryptedContent = if (file.encrypted) {
            AesHelper.decryptString(pinHash!!.pinHash, fileContent)
        } else {
            fileContent
        }

        val snapshot = json.decodeFromString(StorageSnapshot.serializer(), decryptedContent)
        return StorageSnapshotWithMeta(
            snapshot = snapshot,
            absolutePath = file.absolutePath,
            editTimestampUtc = snapshot.timestamp,
            encrypted = file.encrypted
        )
    }

    override suspend fun encode(snapshot: StorageSnapshotWithMeta, file: StorageFile, pinHash: PinHash?): StorageFileExternalContent {
        error("TDS00 encoder is no longer supported")
    }

    private suspend fun getTimestampUnsafe(file: StorageFile, pinHash: PinHash?): Long {
        val tokens = tokensProvider(file).toList()
        if (tokens.size != 1) {
            error("Not TDS00")
        }
        val fileContent = tokens[0]
        if (fileContent.isEmpty()) {
            return 0L
        }

        val decryptedContent = if (file.encrypted) {
            AesHelper.decryptString(pinHash!!.pinHash, fileContent)
        } else {
            fileContent
        }
        return json.decodeFromString(StorageTimestampSnapshot.serializer(), decryptedContent).timestamp
    }

    @Serializable
    private class StorageTimestampSnapshot(@SerialName("t") val timestamp: Long)
}
