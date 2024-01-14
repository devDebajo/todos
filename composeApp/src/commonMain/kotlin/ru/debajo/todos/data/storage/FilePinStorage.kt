package ru.debajo.todos.data.storage

import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.security.HashUtils
import ru.debajo.todos.security.SecuredPreferences

class FilePinStorage(
    private val securedPreferences: SecuredPreferences,
    private val fileCodecHelper: FileCodecHelper,
) {
    suspend fun get(file: StorageFile): PinHash? {
        if (!fileCodecHelper.isEncrypted(file)) {
            return null
        }

        return securedPreferences.getString(createKey(file))?.let { PinHash(it) }
    }

    suspend fun save(file: StorageFile, hash: PinHash) {
        if (!fileCodecHelper.isEncrypted(file)) {
            remove(file)
        } else {
            securedPreferences.putString(createKey(file), hash.pinHash)
        }
    }

    suspend fun remove(file: StorageFile) {
        securedPreferences.remove(createKey(file))
    }

    private fun createKey(file: StorageFile): String {
        return "pin_hash_for_file_${HashUtils.getHash(file.absolutePath)}"
    }
}
