package ru.debajo.todos.common

actual fun createRandomUUID(): UUID = UUID(java.util.UUID.randomUUID().toString())
