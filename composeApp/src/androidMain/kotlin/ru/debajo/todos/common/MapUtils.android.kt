package ru.debajo.todos.common

import java.util.concurrent.ConcurrentHashMap

internal actual fun <K, V> syncMutableMap(): MutableMap<K, V> = ConcurrentHashMap()
