package ru.debajo.todos.common

import kotlin.jvm.JvmInline

@JvmInline
value class UUID(private val uuid: String) {

    override fun toString(): String = uuid

    companion object {
        fun randomUUID(): UUID = createRandomUUID()
    }
}

expect fun createRandomUUID(): UUID
