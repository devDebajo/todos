package ru.debajo.todos.data.storage.model

data class StorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String?,
) {
    val nameWithExtension: String = if (extension.isNullOrEmpty()) name else "$name.$extension"
}
