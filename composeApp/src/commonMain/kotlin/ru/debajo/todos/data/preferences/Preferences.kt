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

    suspend fun putInt(key: String, value: Int)
    suspend fun getInt(key: String): Int?

    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String): Boolean?
}

internal class PreferencesSerializationHelper(private val json: Json) {
    suspend fun encodeStringList(value: List<String>): String {
        return withContext(Default) {
            json.encodeToString(ListSerializer(String.serializer()), value)
        }
    }

    suspend fun decodeStringList(json: String): List<String>? {
        return withContext(Default) {
            runCatching {
                this@PreferencesSerializationHelper.json.decodeFromString(ListSerializer(String.serializer()), json)
            }.getOrNull()
        }
    }
}

internal class PreferencesImpl(
    private val settings: Settings,
    private val serializationHelper: PreferencesSerializationHelper,
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
        putString(key, serializationHelper.encodeStringList(value))
    }

    override suspend fun getStringList(key: String): List<String>? {
        val rawJson = getString(key)
        if (rawJson.isNullOrEmpty()) {
            return null
        }
        return serializationHelper.decodeStringList(rawJson)
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

    override suspend fun putInt(key: String, value: Int) {
        withContext(IO) {
            settings.putInt(key, value)
        }
    }

    override suspend fun getInt(key: String): Int? {
        return withContext(IO) {
            settings.getIntOrNull(key)
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        withContext(IO) {
            settings.putBoolean(key, value)
        }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return withContext(IO) {
            settings.getBooleanOrNull(key)
        }
    }
}
