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
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.data.storage.ExternalFileHelperImpl
import ru.debajo.todos.db.TodosDatabase

internal val AndroidModule: Module = module {
    single<Settings> {
        SharedPreferencesSettings(
            delegate = get<Context>().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE),
            commit = true,
        )
    }
    single<ContentResolver> { get<Context>().contentResolver }
    singleOf(::ActivityResultLaunchersHolder)
    single<ExternalFileHelper> {
        val activityResultLaunchersHolder = get<ActivityResultLaunchersHolder>()
        ExternalFileHelperImpl(
            activityResultLaunchersProvider = { activityResultLaunchersHolder.activityResultLaunchers },
            preferences = get(),
            contentResolver = get(),
            appScope = get(),
        )
    }
    single { DriverFactory { AndroidSqliteDriver(TodosDatabase.Schema, get(), "todos.db") } }
}
