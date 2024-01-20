package ru.debajo.todos.di

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults
import ru.debajo.todos.data.preferences.NSUserDefaultsPreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

val IosModule: Module = module {
    factory<FileSelector> { FileSelectorImpl() }
    single<Preferences> { NSUserDefaultsPreferencesImpl(NSUserDefaults(suiteName = "todo_prefs")) }
    factory<BiometricDelegate> { BiometricDelegateImpl() }
    factory<SecuredScreenManager> { SecuredScreenManagerImpl() }
}
