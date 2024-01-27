package ru.debajo.todos.security

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.debajo.todos.data.preferences.Preferences

internal class SecuredPreferencesImpl(
    private val secretProvider: suspend () -> String,
    private val preferences: Preferences,
    private val json: Json,
    coroutineScope: CoroutineScope,
) : SecuredPreferences {

    private val encryptionUnit: MutableStateFlow<Pair<IV, Salt>?> = MutableStateFlow(null)

    init {
        coroutineScope.launch(Default) {
            var iv = preferences.getString(IV_KEY)?.ivFromString()
            if (iv == null) {
                iv = generateIV()
                preferences.putString(IV_KEY, iv.ivToString())
            }

            var salt = preferences.getString(SALT_KEY)?.asSalt()
            if (salt == null) {
                salt = generateSalt()
                preferences.putString(SALT_KEY, salt.salt)
            }

            encryptionUnit.value = iv to salt
        }
    }

    override suspend fun putString(key: String, value: String) {
        val (iv, salt) = awaitEncryptionUnit()
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key, iv, salt)
        val encryptedValue = AesHelper.encryptStringAsync(secret, value, iv, salt)
        preferences.putString(encryptedKey, encryptedValue)
    }

    override suspend fun getString(key: String): String? {
        val (iv, salt) = awaitEncryptionUnit()
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key, iv, salt)
        val encryptedValue = preferences.getString(encryptedKey) ?: return null
        return AesHelper.decryptStringAsync(secret, encryptedValue, iv, salt)
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
        val (iv, salt) = awaitEncryptionUnit()
        val secret = secretProvider()
        val encryptedKey = AesHelper.encryptStringAsync(secret, key, iv, salt)
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

    private suspend fun awaitEncryptionUnit(): Pair<IV, Salt> {
        return encryptionUnit.filterNotNull().first()
    }

    private companion object {
        const val IV_KEY: String = "spi"
        const val SALT_KEY: String = "sps"
    }
}
