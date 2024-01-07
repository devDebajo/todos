package ru.debajo.todos.security

import ru.debajo.todos.data.preferences.Preferences

interface SecuredPreferences : Preferences {
    suspend fun putStringList(key: String, value: List<String>)

    suspend fun getStringList(key: String): List<String>?
}
