package ru.debajo.todos.security

import kotlin.jvm.JvmInline

@JvmInline
value class EncryptedPinHash(val encryptedPinHash: String)
