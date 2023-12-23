package ru.debajo.todos.data.storage

import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.StateFlow

interface ExternalFileHelper {
    val fileUri: StateFlow<String?>
    suspend fun openOutputStream(): OutputStream
    suspend fun openInputStream(): InputStream
    fun create()
    fun selectFile()
    suspend fun offer(uri: String): Boolean
}
