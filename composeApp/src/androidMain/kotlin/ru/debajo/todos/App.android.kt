package ru.debajo.todos

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.ui.App

class AndroidApp : Application(), CoroutineScope by CoroutineScope(SupervisorJob()) {
    companion object {
        lateinit var INSTANCE: AndroidApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        startKoin {
            modules(
                module {
                    single<Context> { this@AndroidApp }
                    single<CoroutineScope> { this@AndroidApp }
                },
                CommonModule
            )
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        cancel()
    }
}

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
