package ru.debajo.todos.auth

import ru.debajo.todos.data.preferences.Preferences

class SecurityManager(
    private val preferences: Preferences,
) {

    val authType: AuthType = TODO()

    suspend fun getCurrentPinHash(): PinHash {
        TODO()
    }

    suspend fun offer(pin: Pin): Boolean {
        TODO()
    }
}
