package ru.debajo.todos

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.data.storage.ExternalFileHelper
import ru.debajo.todos.di.ActivityResultLaunchersHolder
import ru.debajo.todos.di.AndroidModule
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.App
import ru.debajo.todos.ui.AppScreen
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.NavigatorMediator

class AndroidApp : Application(), CoroutineScope by CoroutineScope(SupervisorJob()) {

    override fun onCreate() {
        super.onCreate()
        initDi()
        initLog()
        startProcess()
    }

    override fun onTerminate() {
        super.onTerminate()
        cancel()
    }

    private fun initDi() {
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
    }

    private fun initLog() {
        Napier.base(DebugAntilog())
    }

    private fun startProcess() {
        val scope = getFromDi<CoroutineScope>()
        val databaseSnapshotWorker = getFromDi<DatabaseSnapshotWorker>()
        scope.launch { databaseSnapshotWorker.doWork() }
    }
}

class AppActivity : ComponentActivity() {

    private val activityResultLaunchers: ActivityResultLaunchers = ActivityResultLaunchers(this)
    private val externalFileHelper: ExternalFileHelper by lazy { getFromDi() }
    private val navigatorMediator: NavigatorMediator by lazy { getFromDi() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getFromDi<ActivityResultLaunchersHolder>().activityResultLaunchers = activityResultLaunchers
        tryToExtractUri(intent)
        setContent {
            CompositionLocalProvider(
                LocalNavigatorMediator provides remember { navigatorMediator }
            ) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tryToExtractUri(intent)
    }

    private fun tryToExtractUri(intent: Intent) {
        val data = intent.data ?: return
        lifecycleScope.launch {
            if (externalFileHelper.offer(data.toString())) {
                navigatorMediator.navigate(AppScreen.SelectFile)
            }
        }
    }
}
