package ru.debajo.todos.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.storage.StorageFilesList
import ru.debajo.todos.di.ActivityResultLaunchersHolder
import ru.debajo.todos.di.AndroidModule
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.di.inject
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManagerImpl
import ru.debajo.todos.ui.theme.AppTheme

internal class AndroidApp : Application(), CoroutineScope by CoroutineScope(SupervisorJob()) {

    private val commonApplication: CommonApplication by inject()

    override fun onCreate() {
        super.onCreate()
        initDi()
        commonApplication.onCreate()
        initForeground()
    }

    private fun initForeground() {
        val handler = Handler(Looper.getMainLooper())
        val fileSession = getFromDi<FileSession>()
        fileSession.addOnOpenListener {
            handler.post { FileSessionService.show(this) }
        }.addOnCloseListener {
            handler.post { FileSessionService.stop(this) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(AppLocalReceiver(), AppLocalReceiver.intentFilter(), RECEIVER_NOT_EXPORTED)
        } else @SuppressLint("UnspecifiedRegisterReceiverFlag") {
            registerReceiver(AppLocalReceiver(), AppLocalReceiver.intentFilter())
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

internal class AppActivity : FragmentActivity() {

    private val notificationManager: TodosNotificationManager by inject()
    private val commonApplication: CommonApplication by inject()
    private val activityResultLaunchers: ActivityResultLaunchers = ActivityResultLaunchers(this)
    private val storageFilesList: StorageFilesList by inject()
    private val navigatorMediator: NavigatorMediator by inject()
    private val securedScreenManagerImpl: SecuredScreenManagerImpl by inject()
    private val appUiLifecycle: AppUiLifecycle by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUiLifecycle.onCreate()
        securedScreenManagerImpl.onCreate()
        getFromDi<ActivityResultLaunchersHolder>().activityResultLaunchers = activityResultLaunchers
        tryToExtractUri(intent)
        setContent {
            AppTheme {
                notificationManager.RequestPermission()
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

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            appUiLifecycle.onDestroy()
        }
    }

    private fun tryToExtractUri(intent: Intent) {
        val data = intent.data ?: return
        lifecycleScope.launch {
            if (storageFilesList.tryAddFile(data.toString())) {
                navigatorMediator.navigate(AppScreen.SelectFile())
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, AppActivity::class.java)
    }
}
