package ru.debajo.todos.di

import org.koin.java.KoinJavaComponent

actual inline fun <reified T> getFromDi(): T = KoinJavaComponent.get(T::class.java)
