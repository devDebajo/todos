package ru.debajo.todos.app

import ru.debajo.todos.strings.R as CommonR
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.debajo.todos.R
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.common.formatKmp
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.di.inject

internal class FileSessionService : Service(), CoroutineScope by CoroutineScope(Main) {

    private val fileSession: FileSession by inject()
    private val notificationManager: TodosNotificationManager by inject()
    private val appUiLifecycle: AppUiLifecycle by inject()
    private val databaseSnapshotSaver: DatabaseSnapshotSaver by inject()
    private val securityManager: AppSecurityManager by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager.ensureChannelCreated(TodosNotificationChannel.FileSession)
        startForeground(buildNotification())
        listenUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun listenUi() {
        launch {
            appUiLifecycle.hasUi.collectLatest { hasUi ->
                if (!hasUi) {
                    ticker(NOTIFICATION_ALIVE_WITHOUT_UI_SEC) {
                        notificationManager.notify(NOTIFICATION_ID, buildNotification(it))
                    }
                    withContext(IO) {
                        databaseSnapshotSaver.save()
                        securityManager.logout()
                        fileSession.close()
                    }
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildNotification())
                }
            }
        }
    }

    private suspend fun ticker(
        count: Int,
        interval: Long = 1000,
        block: (Int) -> Unit,
    ) {
        repeat(count) {
            block(count - it)
            delay(interval)
        }
    }

    private fun buildNotification(leftSeconds: Int? = null): Notification {
        val builder = notificationManager.newNotificationBuilder(TodosNotificationChannel.FileSession)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(CommonR.strings.fileSessionNotificationMessage.formatKmp(fileSession.currentFile?.nameWithExtension.toString()))

        if (leftSeconds != null) {
            builder.setContentText(CommonR.strings.fileSessionNotificationAutoClose.formatKmp(leftSeconds.toString()))
        }

        return builder.setContentIntent(
            AppActivity.createIntent(this)
                .toPending(this, 0, PendingIntentType.ACTIVITY)
        )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                CommonR.strings.fileSessionNotificationClose,
                AppLocalReceiver.closeFilePending(this)
            )
            .setDeleteIntent(AppLocalReceiver.closeFilePending(this))
            .build()
    }

    private fun startForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val NOTIFICATION_ID: Int = 543675376
        private const val NOTIFICATION_ALIVE_WITHOUT_UI_SEC: Int = 10

        fun show(context: Context) {
            if (!TodosNotificationManager.hasNotificationPermission(context)) {
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(createIntent(context))
            } else {
                context.startService(createIntent(context))
            }
        }

        fun stop(context: Context) {
            context.stopService(createIntent(context))
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, FileSessionService::class.java)
        }
    }
}

internal enum class PendingIntentType { BROADCAST, ACTIVITY }

internal fun Intent.toPending(context: Context, requestCode: Int, type: PendingIntentType): PendingIntent {
    val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    return when (type) {
        PendingIntentType.BROADCAST -> PendingIntent.getBroadcast(context, requestCode, this, flags)
        PendingIntentType.ACTIVITY -> PendingIntent.getActivity(context, requestCode, this, flags)
    }
}
