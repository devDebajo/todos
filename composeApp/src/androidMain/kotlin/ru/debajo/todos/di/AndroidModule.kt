package ru.debajo.todos.di

import android.content.ContentResolver
import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.data.storage.ExternalFileHelperImpl

internal val AndroidModule: Module = module {
    single { get<Context>().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE) }
    single<ContentResolver> { get<Context>().contentResolver }
    single { ActivityResultLaunchersHolder() }
    single<ExternalFileHelper> {
        val activityResultLaunchersHolder = get<ActivityResultLaunchersHolder>()
        ExternalFileHelperImpl(
            activityResultLaunchersProvider = { activityResultLaunchersHolder.activityResultLaunchers },
            sharedPreferences = get(),
            contentResolver = get(),
            appScope = get(),
        )
    }
}
