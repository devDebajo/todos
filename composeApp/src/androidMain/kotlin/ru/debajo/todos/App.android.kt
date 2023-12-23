package ru.debajo.todos

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.di.ActivityResultLaunchersHolder
import ru.debajo.todos.di.AndroidModule
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
                AndroidModule,
                CommonModule
            )
        }

        val scope = get<CoroutineScope>(CoroutineScope::class.java)
        val databaseSnapshotWorker = get<DatabaseSnapshotWorker>(DatabaseSnapshotWorker::class.java)
        scope.launch { databaseSnapshotWorker.doWork() }
    }

    override fun onTerminate() {
        super.onTerminate()
        cancel()
    }
}

class AppActivity : ComponentActivity() {

    private val activityResultLaunchers: ActivityResultLaunchers = ActivityResultLaunchers(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        get<ActivityResultLaunchersHolder>(ActivityResultLaunchersHolder::class.java)
            .activityResultLaunchers = activityResultLaunchers

        setContent { App() }
    }
}
