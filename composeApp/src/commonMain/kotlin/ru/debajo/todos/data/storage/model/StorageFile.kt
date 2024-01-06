package ru.debajo.todos.data.storage.model

data class StorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String,
) {
    val nameWithExtension: String = "$name.$extension"
    val encrypted: Boolean = extension == EncryptedExtension

    val isValidExtension: Boolean
        get() = extension == EncryptedExtension || extension == NotEncryptedExtension

    companion object {
        const val EncryptedExtension: String = "tdsc"
        const val NotEncryptedExtension: String = "tds"
    }
}
