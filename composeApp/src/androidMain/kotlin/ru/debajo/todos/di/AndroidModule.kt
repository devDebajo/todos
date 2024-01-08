package ru.debajo.todos.di

import android.content.ContentResolver
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.preferences.SharedPreferencesImpl
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FileHelperImpl
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

internal val AndroidModule: Module = module {
    single<Preferences> {
        SharedPreferencesImpl(
            get<Context>().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        )
    }
    single<ContentResolver> { get<Context>().contentResolver }
    singleOf(::ActivityResultLaunchersHolder)
    factory<FileSelector> {
        val activityResultLaunchersHolder = get<ActivityResultLaunchersHolder>()
        FileSelectorImpl(
            activityResultLaunchersProvider = { activityResultLaunchersHolder.activityResultLaunchers },
            contentResolver = get(),
            fileHelper = get(),
        )
    }
    factory<FileHelper> { FileHelperImpl(get()) }
    single { DriverFactory { AndroidSqliteDriver(TodosDatabase.Schema, get(), "todos.db") } }
    factory<BiometricDelegate> {
        val activityResultLaunchersHolder = get<ActivityResultLaunchersHolder>()
        BiometricDelegateImpl(
            applicationContext = get(),
            activityResultLaunchersProvider = { activityResultLaunchersHolder.activityResultLaunchers },
            preferences = get(),
        )
    }
    single {
        val activityResultLaunchersHolder = get<ActivityResultLaunchersHolder>()
        SecuredScreenManagerImpl(
            activityResultLaunchersProvider = { activityResultLaunchersHolder.activityResultLaunchers }
        )
    }.bind<SecuredScreenManager>()
}
