package ru.debajo.todos.security

import ru.debajo.todos.auth.Pin
import ru.debajo.todos.auth.PinHash

expect object HashUtils {
    fun getHash(input: String): String
}

fun HashUtils.hashPin(pin: Pin): PinHash = PinHash(getHash(pin.pin))
