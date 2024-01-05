package ru.debajo.todos.di

import android.content.ContentResolver
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FileHelperImpl
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl

internal val AndroidModule: Module = module {
    single<Settings> {
        SharedPreferencesSettings(
            delegate = get<Context>().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE),
            commit = true,
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
}
