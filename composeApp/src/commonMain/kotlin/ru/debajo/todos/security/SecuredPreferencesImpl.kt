package ru.debajo.todos.security

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import ru.debajo.todos.data.preferences.PreferencesSerializationHelper

internal class SecuredPreferencesImpl(
    private val secretProvider: suspend () -> String,
    private val settings: Settings,
    private val serializationHelper: PreferencesSerializationHelper,
) : SecuredPreferences {

    override suspend fun putString(key: String, value: String) {
        val secret = secretProvider()
        val encryptedKey = AesHelper.encrypt(secret, key)
        val encryptedValue = AesHelper.encrypt(secret, value)
        withContext(IO) {
            settings.putString(encryptedKey, encryptedValue)
        }
    }

    override suspend fun getString(key: String): String? {
        val secret = secretProvider()
        val encryptedKey = AesHelper.encrypt(secret, key)
        val encryptedValue = withContext(IO) { settings.getStringOrNull(encryptedKey) } ?: return null
        return AesHelper.decrypt(secret, encryptedValue)
    }

    override suspend fun putStringList(key: String, value: List<String>): Unit = putString(key, serializationHelper.encodeStringList(value))

    override suspend fun getStringList(key: String): List<String>? {
        val listJson = getString(key) ?: return null
        return serializationHelper.decodeStringList(listJson)
    }

    override suspend fun putLong(key: String, value: Long): Unit = putString(key, value.toString())

    override suspend fun getLong(key: String): Long? = getString(key)?.toLongOrNull()

    override suspend fun putInt(key: String, value: Int): Unit = putString(key, value.toString())

    override suspend fun getInt(key: String): Int? = getString(key)?.toIntOrNull()

    override suspend fun putBoolean(key: String, value: Boolean): Unit = putString(key, value.toString())

    override suspend fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()
}
