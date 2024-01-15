package ru.debajo.todos.data.storage.codec

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta

typealias StorageFileExternalContent = String

typealias StorageFileToken = String

typealias TokensProvider = (StorageFile) -> Flow<StorageFileToken>

class FileCodecHelper(
    private val json: Json,
    private val fileHelper: FileHelper,
) : StorageFileCodec {

    suspend fun canDecryptFile(file: StorageFile, pinHash: PinHash?): Boolean {
        return runCatchingAsync { getDecoder(file)?.getTimestamp(file, pinHash) != null }
            .getOrElse { false }
    }

    suspend fun isFileReadyToRead(file: StorageFile, pinHash: PinHash?): FileReadReadiness {
        if (!fileHelper.canRead(file)) {
            return FileReadReadiness.NoPermission
        }
        val decoder = getDecoder(file) ?: return FileReadReadiness.UnknownFormat
        if (!decoder.isEncrypted(file)) {
            return FileReadReadiness.Ready
        }

        pinHash ?: return FileReadReadiness.NoPin
        return if (canDecryptFile(file, pinHash)) {
            FileReadReadiness.Ready
        } else {
            FileReadReadiness.NoPin
        }
    }

    override suspend fun isEncrypted(file: StorageFile): Boolean {
        return requireDecoder(file).isEncrypted(file)
    }

    override suspend fun getTimestamp(file: StorageFile, pinHash: PinHash?): Long {
        return requireDecoder(file).getTimestamp(file, pinHash)
    }

    override suspend fun decode(file: StorageFile, pinHash: PinHash?): StorageSnapshotWithMeta {
        return requireDecoder(file).decode(file, pinHash)
    }

    override suspend fun encode(snapshot: StorageSnapshotWithMeta, file: StorageFile, pinHash: PinHash?): StorageFileExternalContent {
        return getEncoder().encode(snapshot, file, pinHash)
    }

    private fun getEncoder(): StorageFileEncoder = StorageFileCodec01(json, ::getTokens)

    private fun getTokens(file: StorageFile): Flow<String> {
        return fileHelper.openInputStream(file).lineFlow()
    }

    private suspend fun getFileFormat(file: StorageFile): String? {
        return when (getTokens(file).first()) {
            Tds01 -> Tds01
            else -> null
        }
    }


    private fun getDecoder(format: String): StorageFileDecoder? {
        return when (format) {
            Tds01 -> StorageFileCodec01(json, ::getTokens)
            else -> null
        }
    }

    private suspend fun getDecoder(file: StorageFile): StorageFileDecoder? {
        val format = getFileFormat(file) ?: return null
        return getDecoder(format)!!
    }

    private suspend fun requireDecoder(file: StorageFile): StorageFileDecoder {
        return getDecoder(file) ?: error("Could not file decoder for file: ${file.absolutePath}")
    }

    enum class FileReadReadiness {
        NoPermission,
        UnknownFormat,
        Ready,
        NoPin
    }

    private companion object {
        const val Tds01: String = StorageFileCodec01.Tds01
    }
}
