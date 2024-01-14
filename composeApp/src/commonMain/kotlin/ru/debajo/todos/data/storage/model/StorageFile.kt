package ru.debajo.todos.data.storage.model

data class StorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String?,
) {
    val nameWithExtension: String = if (extension.isNullOrEmpty()) name else "$name.$extension"

    companion object {
        @Deprecated("Deprecated file standard")
        const val EncryptedExtension: String = "tdsc"

        @Deprecated("Deprecated file standard")
        const val NotEncryptedExtension: String = "tds"
    }
}

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated("Deprecated file standard")
val StorageFile.encrypted: Boolean
    get() = extension == StorageFile.EncryptedExtension
