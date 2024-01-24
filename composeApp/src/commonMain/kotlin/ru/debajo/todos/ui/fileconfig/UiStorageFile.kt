package ru.debajo.todos.ui.fileconfig

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import ru.debajo.todos.common.formatDateTimeWithSeconds
import ru.debajo.todos.data.storage.model.StorageFile

@Immutable
data class UiStorageFile(
    val absolutePath: String,
    val name: String,
    val extension: String?,
    val encrypted: Boolean,
    val edited: Instant,
) {
    val nameWithExtension: String = if (extension.isNullOrEmpty()) name else "$name.$extension"
    val editedFormatted: String = edited.formatDateTimeWithSeconds()
}

fun UiStorageFile.toStorageFile(): StorageFile {
    return StorageFile(
        absolutePath = absolutePath,
        name = name,
        extension = extension,
    )
}
