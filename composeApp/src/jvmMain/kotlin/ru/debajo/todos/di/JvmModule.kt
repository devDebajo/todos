package ru.debajo.todos.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.data.storage.ExternalFileHelperImpl

internal val JvmModule: Module = module {
    single<ExternalFileHelper> {
        ExternalFileHelperImpl()
    }
}
