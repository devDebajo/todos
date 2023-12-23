package ru.debajo.todos.di

import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

val CommonModule: Module = module {
    single {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
}
