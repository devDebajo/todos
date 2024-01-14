package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Immutable
import ru.debajo.todos.data.storage.model.StorageFile

@Immutable
data class UiStorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String?,
    val encrypted: Boolean,
) {
    val nameWithExtension: String = if (extension.isNullOrEmpty()) name else "$name.$extension"
}

fun UiStorageFile.toStorageFile(): StorageFile {
    return StorageFile(
        absolutePath = absolutePath,
        name = name,
        extension = extension,
    )
}
