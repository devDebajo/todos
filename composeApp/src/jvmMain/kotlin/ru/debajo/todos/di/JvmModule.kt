package ru.debajo.todos.di

import KeyEventHandler
import java.io.File
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.debajo.todos.app.QuitHelper
import ru.debajo.todos.data.preferences.FilePreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

internal val JvmModule: Module = module {
    singleOf(::QuitHelper)
    factory<FileSelector> { FileSelectorImpl() }
    single<Preferences> { FilePreferencesImpl(get(), File("todo_prefs")) }
    singleOf(::KeyEventHandler)
    factory<BiometricDelegate> { BiometricDelegateImpl }
    factory<SecuredScreenManager> { SecuredScreenManagerImpl }
}
