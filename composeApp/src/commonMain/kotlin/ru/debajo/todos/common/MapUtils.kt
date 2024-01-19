package ru.debajo.todos.common

internal expect fun <K, V> syncMutableMap(): MutableMap<K, V>
