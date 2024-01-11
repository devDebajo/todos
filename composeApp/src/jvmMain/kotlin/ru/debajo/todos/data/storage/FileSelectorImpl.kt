package ru.debajo.todos.data.storage

import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlin.coroutines.suspendCoroutine
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.strings.R

internal class FileSelectorImpl : FileSelector {

    override suspend fun create(name: String, extension: String): StorageFile? {
        val file: File = suspendCoroutine { continuation ->
            showSaveDialog(name, extension) { continuation.resumeWith(Result.success(it)) }
        } ?: return null

        return StorageFile(
            absolutePath = file.absolutePath,
            name = file.name,
            extension = file.extension
        )
    }

    override suspend fun select(): StorageFile? {
        val file: File = suspendCoroutine { continuation ->
            showLoadDialog { continuation.resumeWith(Result.success(it)) }
        } ?: return null

        return StorageFile(
            absolutePath = file.absolutePath,
            name = file.name,
            extension = file.extension
        )
    }

    private fun showSaveDialog(name: String, extension: String, callback: (File?) -> Unit) {
        val dialog = object : FileDialog(null as Frame?, R.strings.createFile, SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value && !directory.isNullOrEmpty() && !file.isNullOrEmpty()) {
                    val uri = "$directory$file.$extension"
                    val file = File(uri)
                    runCatching { file.delete() }
                    runCatching { file.createNewFile() }
                    callback(file)
                } else {
                    callback(null)
                }
            }
        }

        dialog.file = name
        dialog.isEnabled = false
        dialog.isVisible = true
    }

    private fun showLoadDialog(callback: (File?) -> Unit) {
        object : FileDialog(null as Frame?, R.strings.selectFile, LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value && !directory.isNullOrEmpty() && !file.isNullOrEmpty()) {
                    val uri = "$directory$file"
                    val file = File(uri)
                    callback(file)
                } else {
                    callback(null)
                }
            }
        }.isVisible = true
    }
}
