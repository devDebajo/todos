package ru.debajo.todos.data.storage

import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.common.runCatchingAsync

class DatabaseSnapshotWorker(
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val appLifecycle: AppLifecycle,
) {
    suspend fun doWork() {
        supervisorScope {
            val foreverWorker = launch {
                while (true) {
                    delay(SAVE_DELAY_MS)
                    runCatchingAsync { databaseSnapshotSaver.save() }
                }
            }
            val pausedWorker = launch {
                appLifecycle.state.collect {
                    if (it == AppLifecycle.State.Paused) {
                        runCatchingAsync { databaseSnapshotSaver.save(ignorePaused = true) }
                    }
                }
            }

            listOf(foreverWorker, pausedWorker)
        }.joinAll()
    }

    private companion object {
        const val SAVE_DELAY_MS: Long = 15_000L
    }
}
