package ru.debajo.todos.data.storage

import kotlinx.coroutines.delay
import ru.debajo.todos.common.runCatchingAsync

internal class DatabaseSnapshotWorker(
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
) {
    suspend fun doWork() {
        while (true) {
            delay(SAVE_DELAY_MS)
            runCatchingAsync { databaseSnapshotSaver.save() }
        }
    }

    private companion object {
        const val SAVE_DELAY_MS: Long = 5 * 60 * 1000 // 5 minutes
    }
}
