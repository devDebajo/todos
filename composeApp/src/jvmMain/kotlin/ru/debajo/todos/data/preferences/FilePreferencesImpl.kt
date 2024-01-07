package ru.debajo.todos.data.preferences

import io.github.aakira.napier.Napier
import java.io.File
import kotlin.concurrent.Volatile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

internal class FilePreferencesImpl(
    private val json: Json,
    private val file: File,
) : Preferences {

    private val mutex: Mutex = Mutex()

    @Volatile
    private var current: Map<String, JsonElement>? = null

    override suspend fun putString(key: String, value: String) = update {
        this[key] = JsonPrimitive(value)
    }

    override suspend fun getString(key: String): String? = extract {
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
    }

    override suspend fun putLong(key: String, value: Long) = update {
        this[key] = JsonPrimitive(value)
    }

    override suspend fun getLong(key: String): Long? = extract {
        (this[key] as? JsonPrimitive)?.longOrNull
    }

    override suspend fun putInt(key: String, value: Int) = update {
        this[key] = JsonPrimitive(value)
    }

    override suspend fun getInt(key: String): Int? = extract {
        (this[key] as? JsonPrimitive)?.intOrNull
    }

    override suspend fun putBoolean(key: String, value: Boolean) = update {
        this[key] = JsonPrimitive(value)
    }

    override suspend fun getBoolean(key: String): Boolean? = extract {
        (this[key] as? JsonPrimitive)?.booleanOrNull
    }

    override suspend fun remove(key: String) = update {
        remove(key)
    }

    private suspend fun update(block: MutableMap<String, JsonElement>.() -> Unit) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val currentJsonObject = requireLoaded().toMutableMap()
                currentJsonObject.block()
                current = currentJsonObject.toMap().also { saveToFile(it) }
            }
        }
    }

    private suspend fun <T> extract(block: Map<String, JsonElement>.() -> T?): T? {
        return withContext(Dispatchers.IO) {
            mutex.withLock { requireLoaded().block() }
        }
    }

    private fun requireLoaded(): Map<String, JsonElement> {
        return current ?: loadFromFile().also { current = it }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFromFile(): Map<String, JsonElement> {
        if (!file.exists()){
            return emptyMap()
        }
        return runCatching {
            file.inputStream().use {
                json.decodeFromStream(JsonObject.serializer(), it)
            }
        }
            .onFailure { Napier.e("loadFromFile error", it) }
            .getOrElse { emptyMap() }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun saveToFile(current: Map<String, JsonElement>) {
        file.outputStream().use {
            json.encodeToStream(JsonObject.serializer(), JsonObject(current), it)
        }
    }
}
