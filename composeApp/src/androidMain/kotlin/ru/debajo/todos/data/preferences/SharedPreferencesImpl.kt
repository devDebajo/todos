package ru.debajo.todos.data.preferences

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class SharedPreferencesImpl(
    private val sharedPreferences: SharedPreferences,
) : Preferences {
    override suspend fun putString(key: String, value: String) = update {
        putString(key, value)
    }

    override suspend fun getString(key: String): String? = extract {
        ifExist(key) {
            getString(key, "")
        }
    }

    override suspend fun putLong(key: String, value: Long) = update {
        putLong(key, value)
    }

    override suspend fun getLong(key: String): Long? = extract {
        ifExist(key) {
            getLong(key, 0L)
        }
    }

    override suspend fun putInt(key: String, value: Int) = update {
        putInt(key, value)
    }

    override suspend fun getInt(key: String): Int? = extract {
        ifExist(key) {
            getInt(key, 0)
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) = update {
        putBoolean(key, value)
    }

    override suspend fun getBoolean(key: String): Boolean? = extract {
        ifExist(key) {
            getBoolean(key, false)
        }
    }

    override suspend fun remove(key: String) = update {
        remove(key)
    }

    @SuppressLint("ApplySharedPref")
    private suspend fun update(block: SharedPreferences.Editor.() -> Unit) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().also(block).commit()
        }
    }

    private suspend fun <T> extract(block: SharedPreferences.() -> T?): T? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.block()
        }
    }

    private inline fun <T> SharedPreferences.ifExist(key: String, block: () -> T): T? {
        return if (contains(key)) {
            block()
        } else {
            null
        }
    }
}
