package ru.debajo.todos.data.storage.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class StorageTodoItem(
    @SerialName("id")
    val id: String,

    @SerialName("t")
    val text: String,

    @SerialName("ct")
    val createTimestamp: Long,

    @SerialName("ut")
    val updateTimestamp: Long,

    @SerialName("d")
    val done: Boolean,
)
