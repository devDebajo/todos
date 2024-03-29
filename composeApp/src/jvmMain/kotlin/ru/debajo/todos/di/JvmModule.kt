package ru.debajo.todos.di

import HotkeyDetector
import KeyEventHandler
import java.io.File
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.debajo.todos.app.QuitHelper
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.preferences.FilePreferencesImpl
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
import ru.debajo.todos.security.BiometricDelegate
import ru.debajo.todos.security.BiometricDelegateImpl
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl
import ru.debajo.todos.ui.todolist.HotkeyListener

internal val JvmModule: Module = module {
    singleOf(::QuitHelper)
    factory<FileSelector> { FileSelectorImpl() }
    single<Preferences> { FilePreferencesImpl(get(), getPrefsPath()) }
    singleOf(::KeyEventHandler)
    singleOf(::HotkeyDetector).bind<HotkeyListener>()
    factory<BiometricDelegate> { BiometricDelegateImpl }
    factory<SecuredScreenManager> { SecuredScreenManagerImpl }
}

private val appDirectory: File by lazy {
    File(System.getProperty("user.home") + File.separator + ".todos")
        .also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
}

private fun getPrefsPath(): File {
    val fileName = if (isDebug) "todo_prefs_debug" else "todo_prefs"
    return File(appDirectory, fileName)
}
