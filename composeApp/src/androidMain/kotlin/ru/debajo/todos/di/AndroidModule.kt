package ru.debajo.todos.di

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.debajo.todos.app.AppUiLifecycle
import ru.debajo.todos.app.TodosNotificationManager
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.preferences.SharedPreferencesImpl
import ru.debajo.todos.data.storage.FileSelector
import ru.debajo.todos.data.storage.FileSelectorImpl
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
    single { get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    singleOf(::TodosNotificationManager)
    singleOf(::AppUiLifecycle)
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
