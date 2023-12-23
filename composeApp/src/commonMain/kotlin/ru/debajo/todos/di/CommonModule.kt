package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel

val CommonModule: Module = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
    factory { DatabaseSnapshotHelper() }
    single { DatabaseSnapshotSaver(get(), get(), get()) }
    single { DatabaseSnapshotWorker(get()) }
    factory { FileConfigViewModel(get(), get()) }
}
