package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

private const val SNAPSHOT_TIMESTAMP_KEY: String = "t"

@Serializable
data class StorageSnapshot(
    @SerialName(SNAPSHOT_TIMESTAMP_KEY)
    val timestamp: Long = -1,

    @SerialName("gs")
    val groups: List<StorageTodoGroup> = emptyList(),

    @SerialName("ls")
    val links: List<StorageTodoGroupToItemLink> = emptyList(),

    @SerialName("ts")
    val todos: List<StorageTodoItem> = emptyList(),

    @Transient
    val absolutePath: String? = null,
)

@Serializable
class StorageTimestampSnapshot(
    @SerialName(SNAPSHOT_TIMESTAMP_KEY)
    val timestamp: Long = -1,
)
