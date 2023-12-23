package ru.debajo.todos.data.storage

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import ru.debajo.todos.common.runCatchingAsync

class DatabaseSnapshotWorker(
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
) {
    private val updates: MutableSharedFlow<Long> = MutableSharedFlow()

    @OptIn(FlowPreview::class)
    suspend fun doWork() {
        updates.debounce(2000).collect {
            runCatchingAsync {
                databaseSnapshotSaver.save()
            }
        }
    }

    suspend fun onUpdate() {
        updates.emit(System.currentTimeMillis())
    }
}
