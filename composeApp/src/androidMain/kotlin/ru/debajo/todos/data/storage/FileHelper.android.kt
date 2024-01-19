package ru.debajo.todos.data.storage

import ru.debajo.todos.di.getFromDi

internal actual fun createFileHelper(): FileHelper = FileHelperImpl(getFromDi())
