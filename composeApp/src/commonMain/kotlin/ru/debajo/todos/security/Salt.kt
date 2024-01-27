package ru.debajo.todos.security

import kotlin.jvm.JvmInline

@JvmInline
value class Salt(val salt: String)

fun String.asSalt(): Salt = Salt(this)
