package ru.debajo.todos.data.preferences

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

internal class NSUserDefaultsPreferencesImpl(
    private val userDefaults: NSUserDefaults,
) : Preferences {

    override suspend fun putString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            userDefaults.setObject(value, key)
        }
    }

    override suspend fun getString(key: String): String? {
        return withContext(Dispatchers.IO) {
            if (containsKey(key)) {
                userDefaults.stringForKey(key)
            } else {
                null
            }
        }
    }

    override suspend fun putLong(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            userDefaults.setInteger(value, key)
        }
    }

    override suspend fun getLong(key: String): Long? {
        return withContext(Dispatchers.IO) {
            if (containsKey(key)) {
                userDefaults.integerForKey(key)
            } else {
                null
            }
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        putLong(key, value.toLong())
    }

    override suspend fun getInt(key: String): Int? = getLong(key)?.toInt()

    override suspend fun putBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            userDefaults.setBool(value, key)
        }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return withContext(Dispatchers.IO) {
            if (containsKey(key)) {
                userDefaults.boolForKey(key)
            } else {
                null
            }
        }
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            userDefaults.removeObjectForKey(key)
        }
    }

    private fun containsKey(key: String): Boolean = userDefaults.objectForKey(key) != null
}
