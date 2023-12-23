package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.dao.ReplaceDao
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel

val CommonModule: Module = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
    factory { DatabaseSnapshotHelper(get(), get(), get(), get()) }
    single { DatabaseSnapshotSaver(get(), get(), get()) }
    single { DatabaseSnapshotWorker(get()) }
    factory { FileConfigViewModel(get(), get()) }

    single { TodosDatabase(driver = get<DriverFactory>().createDriver()) }
    single { DbTodoGroupDao(get<TodosDatabase>().dbTodoGroupQueries) }
    single { DbTodoGroupToItemLinkDao(get<TodosDatabase>().dbTodoGroupToItemLinkQueries) }
    single { DbTodoItemDao(get<TodosDatabase>().dbTodoItemQueries) }
    single { ReplaceDao() }
}
