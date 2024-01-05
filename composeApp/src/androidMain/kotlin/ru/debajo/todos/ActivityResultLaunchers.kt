package ru.debajo.todos

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

internal class ActivityResultLaunchers(val activity: FragmentActivity) {

    private val eventBus: MutableSharedFlow<Event> = MutableSharedFlow()
    private val createDocumentLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null) {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileCreated(uri)) }
            } else {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileCreateCancelled) }
            }
        }

    private val openDocumentLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileSelected(uri)) }
            } else {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileSelectCancelled) }
            }
        }

    @Deprecated("")
    fun createDocument(fileName: String, callback: (Uri) -> Unit) {
        createDocumentLauncher.launch(fileName)
        activity.lifecycleScope.launch {
            val event = eventBus.filterIsInstance<Event.FileCreated>().firstOrNull()
            if (event != null) {
                callback(event.uri)
            }
        }
    }

    suspend fun createDocument(fileName: String): Uri? {
        createDocumentLauncher.launch(fileName)
        return when (val event = eventBus.filterIsInstance<Event.Create>().firstOrNull()) {
            is Event.FileCreated -> event.uri
            Event.FileCreateCancelled -> null
            null -> null
        }
    }

    @Deprecated("")
    fun selectDocument(callback: (Uri) -> Unit) {
        openDocumentLauncher.launch(arrayOf("*/*"))
        activity.lifecycleScope.launch {
            val event = eventBus.filterIsInstance<Event.FileSelected>().firstOrNull()
            if (event != null) {
                callback(event.uri)
            }
        }
    }

    suspend fun selectDocument(): Uri? {
        openDocumentLauncher.launch(arrayOf("*/*"))
        return when (val event = eventBus.filterIsInstance<Event.Select>().firstOrNull()) {
            is Event.FileSelected -> event.uri
            Event.FileSelectCancelled -> null
            null -> null
        }
    }

    private sealed interface Event {
        sealed interface Create : Event
        sealed interface Select : Event

        class FileCreated(val uri: Uri) : Create
        data object FileCreateCancelled : Create


        class FileSelected(val uri: Uri) : Select
        data object FileSelectCancelled : Select
    }
}
