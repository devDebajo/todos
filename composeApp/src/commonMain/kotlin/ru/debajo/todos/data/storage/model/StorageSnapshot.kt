package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StorageSnapshot(
    @SerialName("gs")
    val groups: List<StorageTodoGroup> = emptyList(),

    @SerialName("ls")
    val links: List<StorageTodoGroupToItemLink> = emptyList(),

    @SerialName("ts")
    val todos: List<StorageTodoItem> = emptyList(),
)
