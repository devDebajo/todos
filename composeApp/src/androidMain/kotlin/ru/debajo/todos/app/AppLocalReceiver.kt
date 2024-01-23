package ru.debajo.todos.app

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.di.inject

internal class AppLocalReceiver : BroadcastReceiver() {

    private val databaseSnapshotSaver: DatabaseSnapshotSaver by inject()
    private val fileSession: FileSession by inject()
    private val coroutineScope: CoroutineScope by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CLOSE_FILE -> coroutineScope.launch {
                databaseSnapshotSaver.save()
                fileSession.close()
            }
        }
    }

    companion object {
        fun intentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(ACTION_CLOSE_FILE)
            }
        }

        fun closeFilePending(context: Context): PendingIntent {
            return Intent(ACTION_CLOSE_FILE)
                .setPackage(context.packageName)
                .toPending(context, 0, PendingIntentType.BROADCAST)
        }

        const val ACTION_CLOSE_FILE: String = "ru.debajo.todos.app.ACTION_CLOSE_FILE"
    }
}
