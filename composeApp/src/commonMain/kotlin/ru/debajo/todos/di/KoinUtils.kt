package ru.debajo.todos.di

expect inline fun <reified T> getFromDi(): T

inline fun <reified T> inject(): Lazy<T> = lazy { getFromDi() }
