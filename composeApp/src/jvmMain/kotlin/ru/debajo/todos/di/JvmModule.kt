package ru.debajo.todos.di

import KeyEventHandler
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.debajo.todos.data.db.DatabaseFilePath
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.preferences.FilePreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

internal val JvmModule: Module = module {
    factory<FileSelector> { FileSelectorImpl() }
    single<Preferences> { FilePreferencesImpl(get(), File("todo_prefs")) }
    single { DriverFactory { JdbcSqliteDriver("jdbc:sqlite:$DatabaseFilePath") } }
    singleOf(::KeyEventHandler)
    factory<BiometricDelegate> { BiometricDelegateImpl }
    factory<SecuredScreenManager> { SecuredScreenManagerImpl }
}
