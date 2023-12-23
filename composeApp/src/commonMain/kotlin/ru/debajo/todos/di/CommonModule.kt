package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.db.createSchema
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.dao.ReplaceDao
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.domain.TodoGroupRepository
import ru.debajo.todos.domain.TodoItemRepository
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.todolist.TodoListViewModel

val CommonModule: Module = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
    factoryOf(::DatabaseSnapshotHelper)
    singleOf(::DatabaseSnapshotSaver)
    singleOf(::DatabaseSnapshotWorker)
    factoryOf(::FileConfigViewModel)
    factoryOf(::TodoListViewModel)

    single {
        val driver = get<DriverFactory>().createDriver()
        val database = TodosDatabase(driver)
        createSchema(driver)
        database
    }
    single { get<TodosDatabase>().dbTodoGroupQueries }
    single { get<TodosDatabase>().dbTodoGroupToItemLinkQueries }
    single { get<TodosDatabase>().dbTodoItemQueries }
    singleOf(::DbTodoGroupDao)
    singleOf(::DbTodoGroupToItemLinkDao)
    singleOf(::DbTodoItemDao)
    singleOf(::ReplaceDao)

    factoryOf(::TodoGroupRepository)
    factoryOf(::TodoItemRepository)
    factoryOf(::TodoItemUseCase)
}
