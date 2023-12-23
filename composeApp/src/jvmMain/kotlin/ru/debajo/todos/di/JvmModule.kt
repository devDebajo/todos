package ru.debajo.todos.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.data.storage.ExternalFileHelperImpl

internal val JvmModule: Module = module {
    single<ExternalFileHelper> { ExternalFileHelperImpl(get(), get()) }
    single<Settings> { PreferencesSettings(Preferences.userRoot().node("todo_prefs")) }
    single { DriverFactory { JdbcSqliteDriver("jdbc:sqlite:todos.db") } }
}
