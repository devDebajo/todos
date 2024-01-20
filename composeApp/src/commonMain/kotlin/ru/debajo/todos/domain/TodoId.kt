package ru.debajo.todos.domain

import kotlin.jvm.JvmInline
import ru.debajo.todos.common.UUID

@JvmInline
value class TodoId(val id: UUID)
