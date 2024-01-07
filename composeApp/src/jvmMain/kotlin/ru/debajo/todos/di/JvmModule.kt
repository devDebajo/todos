package ru.debajo.todos.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.preferences.FilePreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FileHelperImpl
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl

internal val JvmModule: Module = module {
    factory<FileSelector> { FileSelectorImpl() }
    factory<FileHelper> { FileHelperImpl() }
    single<Preferences> { FilePreferencesImpl(get(), File("todo_prefs")) }
    single { DriverFactory { JdbcSqliteDriver("jdbc:sqlite:todos.db") } }
    factory<BiometricDelegate> { BiometricDelegateImpl }
}
