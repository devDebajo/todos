package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.db.createSchema
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.dao.ReplaceDao
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.preferences.PreferencesImpl
import ru.debajo.todos.data.preferences.PreferencesSerializationHelper
import ru.debajo.todos.data.storage.DatabaseChangeListener
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.domain.TodoGroupRepository
import ru.debajo.todos.domain.TodoItemRepository
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.security.SecuredPreferences
import ru.debajo.todos.security.SecuredPreferencesImpl
import ru.debajo.todos.ui.AppLifecycle
import ru.debajo.todos.ui.AppLifecycleMutable
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.onboarding.OnboardingViewModel
import ru.debajo.todos.ui.pin.PinViewModel
import ru.debajo.todos.ui.splash.SplashViewModel
import ru.debajo.todos.ui.todolist.TodoListViewModel

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

    singleOf(::NavigatorMediator)
    factoryOf(::FileConfigViewModel)
    factoryOf(::TodoListViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::SplashViewModel)
    factoryOf(::PinViewModel)

    single {
        val driver = get<DriverFactory>().createDriver()
        val database = TodosDatabase(driver)
        createSchema(driver)
        database
    }
    factoryOf(::PreferencesSerializationHelper)
    factory<Preferences> { PreferencesImpl(get(), get()) }
    factory<SecuredPreferences> {
        val securityManager = get<AppSecurityManager>()
        SecuredPreferencesImpl(
            secretProvider = { securityManager.getCurrentPinHash().pinHash },
            settings = get(),
            serializationHelper = get()
        )
    }
    single { get<TodosDatabase>().dbTodoGroupQueries }
    single { get<TodosDatabase>().dbTodoGroupToItemLinkQueries }
    single { get<TodosDatabase>().dbTodoItemQueries }
    singleOf(::DbTodoGroupDao)
    singleOf(::DbTodoGroupToItemLinkDao)
    singleOf(::DbTodoItemDao)
    singleOf(::ReplaceDao)
    singleOf(::AppSecurityManager)

    factoryOf(::TodoGroupRepository)
    factoryOf(::TodoItemRepository)
    factoryOf(::TodoItemUseCase)

    single { AppLifecycleMutable() }.bind<AppLifecycle>()
}
