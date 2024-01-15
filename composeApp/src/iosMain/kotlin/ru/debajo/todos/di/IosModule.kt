package ru.debajo.todos.di

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults
import ru.debajo.todos.data.db.DatabaseName
import ru.debajo.todos.data.db.DriverFactory
import ru.debajo.todos.data.preferences.NSUserDefaultsPreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.FileHelperImpl
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.db.TodosDatabase
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

val IosModule: Module = module {
    factory<FileSelector> { FileSelectorImpl() }
    factory<FileHelper> { FileHelperImpl() }
    single<Preferences> { NSUserDefaultsPreferencesImpl(NSUserDefaults(suiteName = "todo_prefs")) }
    single { DriverFactory { NativeSqliteDriver(TodosDatabase.Schema, DatabaseName) } }
    factory<BiometricDelegate> { BiometricDelegateImpl() }
    factory<SecuredScreenManager> { SecuredScreenManagerImpl() }
}
