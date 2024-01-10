package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.app.AppLifecycleMutable
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.db.EncryptedSqlDriver
import ru.debajo.todos.data.db.createSchema
import ru.debajo.todos.data.db.dao.DbFilePathDao
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.dao.ReplaceDao
import ru.debajo.todos.data.storage.DatabaseChangeListener
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.data.storage.FilePinStorage
import ru.debajo.todos.data.storage.StorageFileManager
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.domain.TodoGroupRepository
import ru.debajo.todos.domain.TodoItemRepository
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.security.EncryptFileHelper
import ru.debajo.todos.security.SecuredPreferences
import ru.debajo.todos.security.SecuredPreferencesImpl
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.newpin.NewPinViewModel
import ru.debajo.todos.ui.onboarding.OnboardingViewModel
import ru.debajo.todos.ui.pin.PinViewModel
import ru.debajo.todos.ui.splash.SplashViewModel
import ru.debajo.todos.ui.todolist.TodoListViewModel

private val TodosDatabaseQualifier: Qualifier = qualifier("TodosDatabase")
private val DbTodoGroupQueriesQualifier: Qualifier = qualifier("DbTodoGroupQueries")
private val DbTodoGroupToItemLinkQueriesQualifier: Qualifier = qualifier("DbTodoGroupToItemLinkQueries")
private val DbTodoItemQueriesQualifier: Qualifier = qualifier("DbTodoItemQueries")
private val DbFilePathQueriesQualifier: Qualifier = qualifier("DbFilePathQueries")

val CommonModule: Module = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = isDebug
        }
    }
    factoryOf(::DatabaseSnapshotHelper)
    singleOf(::DatabaseSnapshotSaver).bind<DatabaseChangeListener>()
    singleOf(::DatabaseSnapshotWorker)
    singleOf(::StorageFileManager)
    factoryOf(::FilePinStorage)
    factoryOf(::EncryptFileHelper)

    singleOf(::NavigatorMediator)
    factoryOf(::FileConfigViewModel)
    factoryOf(::TodoListViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::SplashViewModel)
    factoryOf(::PinViewModel)
    factoryOf(::NewPinViewModel)

    single<AsyncProvider<TodosDatabase>>(TodosDatabaseQualifier) {
        val securityManager = get<AppSecurityManager>()
        AsyncProvider { securityManager.awaitCurrentPinHash().pinHash }
            .map { secret ->
                val driver = EncryptedSqlDriver(get<DriverFactory>().createDriver(), secret)
                val database = TodosDatabase(driver)
                createSchema(driver)
                database
            }
            .cached()
    }
    factory<SecuredPreferences> {
        val securityManager = get<AppSecurityManager>()
        SecuredPreferencesImpl(
            secretProvider = { securityManager.awaitCurrentPinHash().pinHash },
            preferences = get(),
            json = get()
        )
    }
    single(DbTodoGroupQueriesQualifier) {
        get<AsyncProvider<TodosDatabase>>(TodosDatabaseQualifier)
            .map { it.dbTodoGroupQueries }
            .cached()
    }
    single(DbTodoGroupToItemLinkQueriesQualifier) {
        get<AsyncProvider<TodosDatabase>>(TodosDatabaseQualifier)
            .map { it.dbTodoGroupToItemLinkQueries }
            .cached()
    }
    single(DbTodoItemQueriesQualifier) {
        get<AsyncProvider<TodosDatabase>>(TodosDatabaseQualifier)
            .map { it.dbTodoItemQueries }
            .cached()
    }
    single(DbFilePathQueriesQualifier) {
        get<AsyncProvider<TodosDatabase>>(TodosDatabaseQualifier)
            .map { it.dbFilePathQueries }
            .cached()
    }
    singleOf(::DbTodoGroupDao)
    singleOf(::DbTodoGroupToItemLinkDao)
    singleOf(::DbTodoItemDao)
    singleOf(::ReplaceDao)
    singleOf(::DbFilePathDao)

    single { DbTodoGroupDao(get(DbTodoGroupQueriesQualifier)) }
    single { DbTodoGroupToItemLinkDao(get(DbTodoGroupToItemLinkQueriesQualifier)) }
    single { DbTodoItemDao(get(DbTodoItemQueriesQualifier)) }
    single {
        ReplaceDao(
            todosDatabaseProvider = get(TodosDatabaseQualifier),
            dbTodoGroupQueriesProvider = get(DbTodoGroupQueriesQualifier),
            dbTodoItemQueriesProvider = get(DbTodoItemQueriesQualifier),
            dbTodoGroupToItemLinkQueriesProvider = get(DbTodoGroupToItemLinkQueriesQualifier),
            dbFilePathQueriesProvider = get(DbFilePathQueriesQualifier)
        )
    }
    single { DbFilePathDao(get(DbFilePathQueriesQualifier)) }

    singleOf(::AppSecurityManager)

    factoryOf(::TodoGroupRepository)
    factoryOf(::TodoItemRepository)
    factoryOf(::TodoItemUseCase)

    single { AppLifecycleMutable() }.bind<AppLifecycle>()
}
