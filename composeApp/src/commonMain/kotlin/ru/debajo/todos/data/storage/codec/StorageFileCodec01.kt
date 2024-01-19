package ru.debajo.todos.data.storage.codec

import kotlinx.serialization.json.Json
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta
import ru.debajo.todos.security.AesHelper
import ru.debajo.todos.security.Base64Utils
import ru.debajo.todos.security.decryptString
import ru.debajo.todos.security.decryptStringAsync
import ru.debajo.todos.security.encryptStringAsync

class StorageFileCodec01(
    private val json: Json,
    private val tokensProvider: TokensProvider,
) : StorageFileCodec {
    override suspend fun isEncrypted(file: StorageFile): Boolean {
        return tokensProvider(file).drop(1).first().toEncryptedFlagStrict()
    }

    override suspend fun getTimestamp(file: StorageFile, pinHash: PinHash?): Long {
        val tokens = tokensProvider(file).drop(1).take(2).toList()
        require(tokens.size == 2)
        val encrypted = tokens[0].toEncryptedFlagStrict()
        return tokens[1].toTimestamp(encrypted, pinHash)
    }

    override suspend fun decode(file: StorageFile, pinHash: PinHash?): StorageSnapshotWithMeta {
        val tokens = tokensProvider(file).drop(1).take(3).toList()
        val encrypted = tokens[0].toEncryptedFlagStrict()
        val timestamp = tokens[1].toTimestamp(encrypted, pinHash)
        val rawContent = tokens[2]
        val snapshotJson = if (encrypted) {
            AesHelper.decryptStringAsync(pinHash!!.pinHash, rawContent)
        } else {
            Base64Utils.decodeString(rawContent)
        }
        val snapshot = json.decodeFromString(StorageSnapshot.serializer(), snapshotJson)
        return StorageSnapshotWithMeta(
            snapshot = snapshot,
            absolutePath = file.absolutePath,
            editTimestampUtc = timestamp,
            encrypted = encrypted,
        )
    }


    override suspend fun encode(snapshot: StorageSnapshotWithMeta, file: StorageFile, pinHash: PinHash?): StorageFileExternalContent {
        if (snapshot.encrypted) {
            requireNotNull(pinHash)
        }
        val rawData = json.encodeToString(StorageSnapshot.serializer(), snapshot.snapshot)
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(Tds01)
        stringBuilder.appendLine(if (snapshot.encrypted) "1" else "0")
        stringBuilder.appendLine(
            if (snapshot.encrypted) {
                AesHelper.encryptStringAsync(pinHash!!.pinHash, snapshot.editTimestampUtc.toString())
            } else {
                Base64Utils.encodeString(snapshot.editTimestampUtc.toString())
            }
        )
        stringBuilder.appendLine(
            if (snapshot.encrypted) {
                AesHelper.encryptStringAsync(pinHash!!.pinHash, rawData)
            } else {
                Base64Utils.encodeString(rawData)
            }
        )
        return stringBuilder.toString()
    }

    private fun StorageFileToken.toEncryptedFlagStrict(): Boolean {
        return when (this) {
            "1" -> true
            "0" -> false
            else -> error("Unknown encrypted flag")
        }
    }

    private fun StorageFileToken.toTimestamp(isEncrypted: Boolean, pinHash: PinHash?): Long {
        return if (isEncrypted) {
            AesHelper.decryptString(pinHash!!.pinHash, this).toLong()
        } else {
            Base64Utils.decodeString(this).toLong()
        }
    }

    companion object {
        const val Tds01: String = "TDS01"
    }
}
