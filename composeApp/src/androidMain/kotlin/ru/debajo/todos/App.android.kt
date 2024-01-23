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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.app.CommonApplication
import ru.debajo.todos.data.db.FileSession
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

    private val commonApplication: CommonApplication by inject()

    override fun onCreate() {
        super.onCreate()
        initDi()
        commonApplication.onCreate()
        initForeground()
    }

    private fun initForeground() {
        val fileSession = getFromDi<FileSession>()
        fileSession.addOnOpenListener {
            // TODO start foregroundService
        }.addOnCloseListener {
            // TODO stop foregroundService
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        launch {
            commonApplication.onTerminate()
            cancel()
        }
    }

    private fun initDi() {
        startKoin {
            modules(
                module {
                    single<Context> { this@AndroidApp }
                    single<CoroutineScope> { this@AndroidApp }
                    single { CommonApplication() }
                },
                AndroidModule,
                CommonModule
            )
        }
    }
}

class AppActivity : FragmentActivity() {

    private val commonApplication: CommonApplication by inject()
    private val activityResultLaunchers: ActivityResultLaunchers = ActivityResultLaunchers(this)
    private val storageFileManager: StorageFileManager by inject()
    private val navigatorMediator: NavigatorMediator by inject()
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
                commonApplication.Content()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        tryToExtractUri(intent)
    }

    override fun onPause() {
        super.onPause()
        commonApplication.onPause()
    }

    override fun onResume() {
        super.onResume()
        commonApplication.onResume()
    }

    private fun tryToExtractUri(intent: Intent) {
        val data = intent.data ?: return
        lifecycleScope.launch {
            if (storageFileManager.tryAddFile(data.toString())) {
                navigatorMediator.navigate(AppScreen.SelectFile())
            }
        }
    }
}
