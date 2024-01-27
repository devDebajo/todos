package ru.debajo.todos.security

expect fun randomSalt(): String

expect fun randomIV(): ByteArray
