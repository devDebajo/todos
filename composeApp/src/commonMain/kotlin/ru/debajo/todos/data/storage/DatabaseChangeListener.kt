package ru.debajo.todos.data.storage

interface DatabaseChangeListener {
    suspend fun onUpdate()
}
