package ru.debajo.todos.security

import ru.debajo.todos.auth.Pin
import ru.debajo.todos.auth.PinHash
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.di.AsyncProvider
import ru.debajo.todos.di.cached

expect object HashUtils {
    fun getHash(input: String, salt: String): String

    fun getHash(input: String): String
}

class PinHasher(
    private val preferences: Preferences,
) {
    private val salt: AsyncProvider<Salt> = AsyncProvider { awaitSalt() }.cached()

    // hash(hash(pin) + salt)
    suspend fun hashPin(pin: Pin): PinHash {
        val salt = salt.provide()
        return PinHash(HashUtils.getHash(HashUtils.getHash(pin.pin), salt.salt))
    }

    private suspend fun awaitSalt(): Salt {
        var salt = preferences.getString(SALT_KEY)?.asSalt()
        if (salt == null) {
            salt = generateSalt()
            preferences.putString(SALT_KEY, salt.salt)
        }
        return salt
    }

    private companion object {
        const val SALT_KEY: String = "pinsalt"
    }
}