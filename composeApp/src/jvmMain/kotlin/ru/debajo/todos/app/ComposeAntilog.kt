package ru.debajo.todos.app

import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

internal class ComposeAntilog : Antilog() {

    val logs: SnapshotStateList<String> = SnapshotStateList()

    override fun performLog(priority: LogLevel, tag: String?, throwable: Throwable?, message: String?) {
        val logTag = tag ?: "ComposeAntilog"

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.message}"
            } else {
                message
            }
        } else throwable?.message ?: return

        logs += when (priority) {
            LogLevel.VERBOSE -> "VERBOSE $logTag : $fullMessage"
            LogLevel.DEBUG -> "DEBUG $logTag : $fullMessage"
            LogLevel.INFO -> "INFO $logTag : $fullMessage"
            LogLevel.WARNING -> "WARNING $logTag : $fullMessage"
            LogLevel.ERROR -> "ERROR $logTag : $fullMessage"
            LogLevel.ASSERT -> "ASSERT $logTag : $fullMessage"
        }
    }
}
