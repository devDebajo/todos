package ru.debajo.todos.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ru.debajo.todos.common.hasPermission
import ru.debajo.todos.strings.R

@OptIn(ExperimentalPermissionsApi::class)
internal class TodosNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManager,
) {
    private val permissionGranted: Boolean
        get() = hasNotificationPermission(context)

    @Composable
    fun RequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val hasPermission = remember { mutableStateOf(PERMISSION_UNKNOWN) }
        RequestPermission(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            key = this,
            onDeny = { lifecycle ->
                if (!lifecycle || hasPermission.value != PERMISSION_UNKNOWN) {
                    hasPermission.value = false
                }
            },
            onGrant = { lifecycle ->
                if (!lifecycle || hasPermission.value != PERMISSION_UNKNOWN) {
                    hasPermission.value = true
                }
            },
        )

        if (hasPermission.value == false) {
            BlockingPermissionDialog()
        }
    }

    @Suppress("SameParameterValue")
    @Composable
    @ExperimentalPermissionsApi
    private fun RequestPermission(
        permission: String,
        key: Any,
        onDeny: (lifecycle: Boolean) -> Unit = {},
        onGrant: (lifecycle: Boolean) -> Unit = {},
    ) {
        val onDenyLatest = rememberUpdatedState(onDeny)
        val onGrantLatest = rememberUpdatedState(onGrant)

        val permissionState = rememberPermissionState(permission = permission) { granted ->
            if (granted) {
                onGrantLatest.value(false)
            } else {
                onDenyLatest.value(false)
            }
        }

        LaunchedEffect(key) {
            if (permissionState.status.isGranted) {
                onGrantLatest.value(false)
            } else {
                permissionState.launchPermissionRequest()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event: Lifecycle.Event ->
                if (event.targetState.isAtLeast(Lifecycle.State.RESUMED)) {
                    if (permissionState.status.isGranted) {
                        onGrantLatest.value(true)
                    } else {
                        onDenyLatest.value(true)
                    }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    @Composable
    private fun BlockingPermissionDialog() {
        AlertDialog(
            title = { Text("Доступ к уведомлениям") },
            text = { Text("Вы не дали доступ к уведомлениям. К сожалению, без данного разрешения приложение будет работать некорректно") },
            confirmButton = {
                val context = LocalContext.current
                TextButton(onClick = { context.startActivity(createSettingsIntent(context)) }) {
                    Text("Выдать доступ")
                }
            },
            onDismissRequest = { }
        )
    }

    fun notify(id: Int, notification: Notification) {
        if (permissionGranted) {
            ensureChannelCreated(TodosNotificationChannel.from(notification))
            notificationManager.notify(id, notification)
        }
    }

    fun newNotificationBuilder(channel: TodosNotificationChannel): NotificationCompat.Builder {
        ensureChannelCreated(channel)
        return NotificationCompat.Builder(context, channel.id)
    }

    fun ensureChannelCreated(channel: TodosNotificationChannel) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || channel == TodosNotificationChannel.StubChannel) {
            return
        }
        val channelCreated = runCatching { notificationManager.getNotificationChannel(channel.id) }
            .getOrNull() != null
        if (!channelCreated) {
            prepareChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareChannel(channel: TodosNotificationChannel) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val systemChannel = NotificationChannel(
            channel.id, channel.name, importance
        ).apply {
            this.description = channel.description
        }
        notificationManager.createNotificationChannel(systemChannel)
    }

    private fun createSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                .putExtra("app_package", context.packageName)
                .putExtra("app_uid", context.applicationInfo.uid)
        }
    }

    companion object {
        private val PERMISSION_UNKNOWN: Any = object {}

        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                true
            }
        }
    }
}

internal sealed interface TodosNotificationChannel {
    val id: String

    val name: String

    val description: String

    data object FileSession : TodosNotificationChannel {
        override val id: String = "TODOs_FileSession_NOTIFICATION"
        override val name: String = R.strings.fileSessionNotificationChannelName
        override val description: String = R.strings.fileSessionNotificationChannelDescription
    }

    data object StubChannel : TodosNotificationChannel {
        override val id: String = ""
        override val name: String = ""
        override val description: String = ""
    }

    companion object {
        fun from(notification: Notification): TodosNotificationChannel {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return StubChannel
            }

            return when (notification.channelId) {
                FileSession.id -> FileSession
                else -> StubChannel
            }
        }
    }
}
