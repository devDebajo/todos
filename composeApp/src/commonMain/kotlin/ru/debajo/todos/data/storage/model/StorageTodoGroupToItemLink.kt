package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StorageTodoGroupToItemLink(
    @SerialName("gid")
    val groupId: String,

    @SerialName("tid")
    val todoId: String,
)
