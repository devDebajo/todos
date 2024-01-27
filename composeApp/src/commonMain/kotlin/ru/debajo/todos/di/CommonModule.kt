package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.debajo.todos.app.CommonApplication
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.db.FileSessionManager
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.storage.DatabaseSnapshotHelper
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FileHelperContentCache
import ru.debajo.todos.data.storage.FilePinStorage
import ru.debajo.todos.data.storage.StorageFilesList
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.createFileHelper
import ru.debajo.todos.domain.TodoGroupRepository
import ru.debajo.todos.domain.TodoItemRepository
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.security.SecuredPreferences
import ru.debajo.todos.security.SecuredPreferencesImpl
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.newpin.NewPinViewModel
import ru.debajo.todos.ui.onboarding.OnboardingViewModel
import ru.debajo.todos.ui.pin.PinViewModel
import ru.debajo.todos.ui.splash.SplashViewModel
import ru.debajo.todos.ui.todolist.TodoListViewModel

val CommonModule: Module = module {
    single { CommonApplication() }
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = isDebug
        }
    }
    factoryOf(::DatabaseSnapshotHelper)
    singleOf(::DatabaseSnapshotSaver)
    singleOf(::DatabaseSnapshotWorker)
    singleOf(::StorageFilesList)
    factoryOf(::FilePinStorage)
    singleOf(::FileCodecHelper)
    single<FileHelper> { FileHelperContentCache(createFileHelper()) }
    singleOf(::NavigatorMediator)
    factoryOf(::FileConfigViewModel)
    factoryOf(::TodoListViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::SplashViewModel)
    factoryOf(::PinViewModel)
    factoryOf(::NewPinViewModel)

    factory<SecuredPreferences> {
        val securityManager = get<AppSecurityManager>()
        SecuredPreferencesImpl(
            secretProvider = { securityManager.awaitCurrentPinHash().pinHash },
            preferences = get(),
            json = get()
        )
    }
    singleOf(::DbTodoGroupDao)
    singleOf(::DbTodoGroupToItemLinkDao)
    singleOf(::DbTodoItemDao)
    singleOf(::FileSession)
    singleOf(::FileSessionManager)

    singleOf(::AppSecurityManager)

    factoryOf(::TodoGroupRepository)
    factoryOf(::TodoItemRepository)
    factoryOf(::TodoItemUseCase)
}
