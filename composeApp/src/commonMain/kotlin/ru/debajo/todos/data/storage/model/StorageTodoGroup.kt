package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StorageTodoGroup(
    @SerialName("id")
    val id: String,

    @SerialName("n")
    val name: String,
)
