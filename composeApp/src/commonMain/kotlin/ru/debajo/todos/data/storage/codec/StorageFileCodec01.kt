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
import ru.debajo.todos.security.ivFromString
import ru.debajo.todos.security.ivToString
import ru.debajo.todos.security.randomIV
import ru.debajo.todos.security.randomSalt

/**
 * Tokens:
 * 0 TDS01
 * 1 Encrypted flag <1|0>
 * 2 IV (comma separated, or empty line if not encrypted)
 * 3 Salt (or empty line if not encrypted)
 * 4 Edit timestamp in UTC (encrypted or raw Long) in base64
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
        val tokens = tokensProvider(file).drop(1).take(4).toList()
        require(tokens.size == 2)
        val encrypted = tokens[0].toEncryptedFlagStrict()
        val iv = tokens[1].ivFromString()
        val salt = tokens[2]
        return tokens[4].toTimestamp(encrypted, pinHash, iv, salt)
    }

    override suspend fun decode(file: StorageFile, pinHash: PinHash?): StorageSnapshotWithMeta {
        val tokens = tokensProvider(file).drop(1).take(5).toList()
        val encrypted = tokens[0].toEncryptedFlagStrict()
        val iv = tokens[1].ivFromString()
        val salt = tokens[2]
        val timestamp = tokens[3].toTimestamp(encrypted, pinHash, iv, salt)
        val rawContent = tokens[4]
        val snapshotJson = if (encrypted) {
            AesHelper.decryptStringAsync(pinHash!!.pinHash, rawContent, iv, salt)
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
        val iv: ByteArray
        val salt: String
        if (snapshot.encrypted) {
            iv = randomIV()
            salt = randomSalt()
        } else {
            iv = EmptyByteArray
            salt = ""
        }
        val rawData = json.encodeToString(StorageSnapshot.serializer(), snapshot.snapshot)
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine(Tds01)
        stringBuilder.appendLine(if (snapshot.encrypted) "1" else "0")
        stringBuilder.appendLine(iv.ivToString())
        stringBuilder.appendLine(salt)
        stringBuilder.appendLine(
            if (snapshot.encrypted) {
                AesHelper.encryptStringAsync(
                    secret = pinHash!!.pinHash,
                    rawData = snapshot.editTimestampUtc.toString(),
                    iv = iv,
                    salt = salt,
                )
            } else {
                Base64Utils.encodeString(snapshot.editTimestampUtc.toString())
            }
        )
        stringBuilder.appendLine(
            if (snapshot.encrypted) {
                AesHelper.encryptStringAsync(pinHash!!.pinHash, rawData, iv, salt)
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

    private fun StorageFileToken.toTimestamp(
        isEncrypted: Boolean,
        pinHash: PinHash?,
        iv: ByteArray,
        salt: String,
    ): Long {
        return if (isEncrypted) {
            AesHelper.decryptString(pinHash!!.pinHash, this, iv, salt).toLong()
        } else {
            Base64Utils.decodeString(this).toLong()
        }
    }

    companion object {
        val EmptyByteArray: ByteArray = byteArrayOf()
        const val Tds01: String = "TDS01"
    }
}
