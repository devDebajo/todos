package ru.debajo.todos

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.app.App
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.app.AppLifecycleMutable
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.data.storage.StorageFileManager
import ru.debajo.todos.di.ActivityResultLaunchersHolder
import ru.debajo.todos.di.AndroidModule
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.di.inject
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl

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
        if (isDebug) {
            Napier.base(DebugAntilog())
        }
    }

    private fun startProcess() {
        val scope = getFromDi<CoroutineScope>()
        val databaseSnapshotWorker = getFromDi<DatabaseSnapshotWorker>()
        scope.launch { databaseSnapshotWorker.doWork() }
    }
}

class AppActivity : FragmentActivity() {

    private val activityResultLaunchers: ActivityResultLaunchers = ActivityResultLaunchers(this)
    private val storageFileManager: StorageFileManager by inject()
    private val navigatorMediator: NavigatorMediator by inject()
    private val appLifecycleMutable: AppLifecycleMutable by inject()
    private val securedScreenManagerImpl: SecuredScreenManagerImpl by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        securedScreenManagerImpl.onCreate()
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

    override fun onPause() {
        super.onPause()
        appLifecycleMutable.updateState(AppLifecycle.State.Paused)
    }

    override fun onResume() {
        super.onResume()
        appLifecycleMutable.updateState(AppLifecycle.State.Resumed)
    }

    // TODO холодный запуск - список не обновляется
    // TODO файл очень долго открывается, видимо дешифровка долгая, надо оптимизировать как-то
    // TODO чистить базу, при входе
    private fun tryToExtractUri(intent: Intent) {
        val data = intent.data ?: return
        lifecycleScope.launch {
            if (storageFileManager.tryAddFile(data.toString())) {
                navigatorMediator.navigate(AppScreen.SelectFile())
            }
        }
    }
}
