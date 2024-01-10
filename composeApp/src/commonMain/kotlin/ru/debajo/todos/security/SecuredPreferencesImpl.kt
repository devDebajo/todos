package ru.debajo.todos.security

import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.debajo.todos.data.preferences.Preferences

internal class SecuredPreferencesImpl(
    private val secretProvider: suspend () -> String,
    private val preferences: Preferences,
    private val json: Json,
) : SecuredPreferences {

    override suspend fun putString(key: String, value: String) {
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key)
        val encryptedValue = AesHelper.encryptStringAsync(secret, value)
        preferences.putString(encryptedKey, encryptedValue)
    }

    override suspend fun getString(key: String): String? {
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key)
        val encryptedValue = preferences.getString(encryptedKey) ?: return null
        return AesHelper.decryptStringAsync(secret, encryptedValue)
    }

    override suspend fun putStringList(key: String, value: List<String>): Unit = putString(key, encodeStringList(value))

    override suspend fun getStringList(key: String): List<String>? {
        val listJson = getString(key) ?: return null
        return decodeStringList(listJson)
    }

    override suspend fun putLong(key: String, value: Long): Unit = putString(key, value.toString())

    override suspend fun getLong(key: String): Long? = getString(key)?.toLongOrNull()

    override suspend fun putInt(key: String, value: Int): Unit = putString(key, value.toString())

    override suspend fun getInt(key: String): Int? = getString(key)?.toIntOrNull()

    override suspend fun putBoolean(key: String, value: Boolean): Unit = putString(key, value.toString())

    override suspend fun getBoolean(key: String): Boolean? = getString(key)?.toBooleanStrictOrNull()

    override suspend fun remove(key: String) {
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key)
        preferences.remove(encryptedKey)
    }

    private suspend fun encodeStringList(value: List<String>): String {
        return withContext(Default) {
            json.encodeToString(ListSerializer(String.serializer()), value)
        }
    }

    private suspend fun decodeStringList(json: String): List<String>? {
        return withContext(Default) {
            runCatching {
                this@SecuredPreferencesImpl.json.decodeFromString(ListSerializer(String.serializer()), json)
            }.getOrNull()
        }
    }
}
