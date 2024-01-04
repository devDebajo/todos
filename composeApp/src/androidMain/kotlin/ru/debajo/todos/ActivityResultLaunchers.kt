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

    fun createDocument(fileName: String, callback: (Uri) -> Unit) {
        createDocumentLauncher.launch(fileName)
        activity.lifecycleScope.launch {
            val event = eventBus.filterIsInstance<Event.FileCreated>().firstOrNull()
            if (event != null) {
                callback(event.uri)
            }
        }
    }

    fun selectDocument(callback: (Uri) -> Unit) {
        openDocumentLauncher.launch(arrayOf("*/*"))
        activity.lifecycleScope.launch {
            val event = eventBus.filterIsInstance<Event.FileSelected>().firstOrNull()
            if (event != null) {
                callback(event.uri)
            }
        }
    }

    sealed interface Event {
        class FileCreated(val uri: Uri) : Event
        data object FileCreateCancelled : Event
        class FileSelected(val uri: Uri) : Event
        data object FileSelectCancelled : Event
    }
}
