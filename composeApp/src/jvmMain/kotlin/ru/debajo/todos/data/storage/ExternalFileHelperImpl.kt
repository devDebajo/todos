package ru.debajo.todos.data.storage

import java.awt.FileDialog
import java.awt.Frame
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExternalFileHelperImpl : ExternalFileHelper {

    private val _fileUri: MutableStateFlow<String?> = MutableStateFlow(null)
    override val fileUri: StateFlow<String?> = _fileUri.asStateFlow()

    override suspend fun openOutputStream(): OutputStream = awaitCancellation()

    override suspend fun openInputStream(): InputStream = awaitCancellation()

    override fun create() = Unit

    override fun selectFile() {
        object : FileDialog(null as Frame?, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                }
            }
        }.isVisible = true
    }

    override suspend fun offer(uri: String): Boolean = false
}

