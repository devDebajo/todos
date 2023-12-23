package ru.debajo.todos

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

internal class ActivityResultLaunchers(private val activity: ComponentActivity) {

    private val eventBus: MutableSharedFlow<Event> = MutableSharedFlow()
    private val createDocumentLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
            if (uri != null) {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileCreated(uri)) }
            } else {
                activity.lifecycleScope.launch { eventBus.emit(Event.FileCreateCancelled) }
            }
        }

    private val openDocumentLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
        openDocumentLauncher.launch("*/*")
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

//    class OpenDocument : ActivityResultContracts.OpenDocument() {
//        override fun createIntent(context: Context, input: Array<String>): Intent {
//            return super.createIntent(context, input).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
//                addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//            }
//        }
//    }
//
//    class CreateDocument(typeString: String) : ActivityResultContracts.CreateDocument(typeString) {
//        override fun createIntent(context: Context, input: String): Intent {
//            return super.createIntent(context, input).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//            }
//        }
//    }
