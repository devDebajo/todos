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
import ru.debajo.todos.security.randomIV
import ru.debajo.todos.security.randomSalt

/**
 * Tokens:
 * 0 TDS01
 * 1 Encrypted flag <1|0>
 * 2 Edit timestamp in UTC (encrypted or raw Long) in base64
 * 3 IV (comma separated, or empty line if not encrypted)
 * 4 Salt (or empty line if not encrypted)
 * 5 File content. Encrypted Json or raw Json in base64
 */
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
        val tokens = tokensProvider(file).drop(1).take(5).toList()
        val encrypted = tokens[0].toEncryptedFlagStrict()
        val timestamp = tokens[1].toTimestamp(encrypted, pinHash)
        val iv = tokens[2].toIv()
        val salt = tokens[3]
        val rawContent = tokens[4]
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
        val iv: ByteArray
        val salt: String
        if (snapshot.encrypted) {
            iv = randomIV()
            salt = randomSalt()
        } else {
            iv = EmptyByteArray
            salt = ""
        }
        stringBuilder.appendLine(iv.joinToString(separator = ",") { it.toString() })
        stringBuilder.appendLine(salt)
        stringBuilder.appendLine(
            if (snapshot.encrypted) {
                AesHelper.encryptStringAsync(pinHash!!.pinHash, rawData)
            } else {
                Base64Utils.encodeString(rawData)
            }
        )
        return stringBuilder.toString()
    }

    private fun StorageFileToken.toIv(): ByteArray {
        if (isEmpty()) {
            return byteArrayOf()
        }
        return split(",").map { it.toByte() }.toByteArray()
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
        val EmptyByteArray: ByteArray = byteArrayOf()
        const val Tds01: String = "TDS01"
    }
}
