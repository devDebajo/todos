package ru.debajo.todos.security

import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FilePinStorage
import ru.debajo.todos.data.storage.model.StorageFile

class EncryptFileHelper(
    private val fileHelper: FileHelper,
    private val filePinStorage: FilePinStorage,
) {
    suspend fun canDecryptFile(file: StorageFile, pinHash: PinHash?): Boolean {
        return runCatchingAsync { decryptFile(file, pinHash) }
            .map { true }
            .getOrElse { false }
    }

    suspend fun isFileReadyToRead(file: StorageFile): FileReadReadiness {
        if (!fileHelper.canRead(file)) {
            return FileReadReadiness.NoPermission
        }
        if (!file.encrypted) {
            return FileReadReadiness.Ready
        }

        val pinHash = filePinStorage.get(file) ?: return FileReadReadiness.NoPin
        return if (canDecryptFile(file, pinHash)) {
            FileReadReadiness.Ready
        } else {
            FileReadReadiness.NoPin
        }
    }

    suspend fun decryptFile(file: StorageFile, pinHash: PinHash?): String {
        val fileContent = fileHelper.openInputStream(file).bufferedReader().readText()
        if (fileContent.isEmpty()) {
            return fileContent
        }
        return if (file.encrypted) {
            AesHelper.decrypt(pinHash!!.pinHash, fileContent)
        } else {
            fileContent
        }
    }

    suspend fun encryptFile(file: StorageFile, content: String, pinHash: PinHash?): String {
        return if (file.encrypted) {
            AesHelper.encrypt(pinHash!!.pinHash, content)
        } else {
            content
        }
    }

    enum class FileReadReadiness {
        NoPermission,
        Ready,
        NoPin
    }
}
