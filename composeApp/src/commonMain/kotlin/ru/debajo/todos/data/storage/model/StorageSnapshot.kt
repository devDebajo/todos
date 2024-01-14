package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorageSnapshot(
    @SerialName("t")
    val timestamp: Long = 0L,

    @SerialName("gs")
    val groups: List<StorageTodoGroup> = emptyList(),

    @SerialName("ls")
    val links: List<StorageTodoGroupToItemLink> = emptyList(),

    @SerialName("ts")
    val todos: List<StorageTodoItem> = emptyList(),
)
