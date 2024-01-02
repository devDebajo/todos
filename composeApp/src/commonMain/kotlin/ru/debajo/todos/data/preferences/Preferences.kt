package ru.debajo.todos.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

interface Preferences {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?

    suspend fun putStringList(key: String, value: List<String>)
    suspend fun getStringList(key: String): List<String>?

    suspend fun putLong(key: String, value: Long)
    suspend fun getLong(key: String): Long?
}

internal class PreferencesImpl(
    private val settings: Settings,
    private val json: Json,
) : Preferences {

    override suspend fun putString(key: String, value: String) {
        withContext(IO) {
            settings.putString(key, value)
        }
    }

    override suspend fun getString(key: String): String? {
        return withContext(IO) {
            settings.getStringOrNull(key)
        }
    }

    override suspend fun putStringList(key: String, value: List<String>) {
        val rawJson = withContext(Default) {
            json.encodeToString(ListSerializer(String.serializer()), value)
        }

        putString(key, rawJson)
    }

    override suspend fun getStringList(key: String): List<String>? {
        val rawJson = getString(key)
        if (rawJson.isNullOrEmpty()) {
            return null
        }
        return withContext(Default) {
            runCatching {
                json.decodeFromString(ListSerializer(String.serializer()), rawJson)
            }.getOrNull()
        }
    }

    override suspend fun putLong(key: String, value: Long) {
        withContext(IO) {
            settings.putLong(key, value)
        }
    }

    override suspend fun getLong(key: String): Long? {
        return withContext(IO) {
            settings.getLongOrNull(key)
        }
    }
}
