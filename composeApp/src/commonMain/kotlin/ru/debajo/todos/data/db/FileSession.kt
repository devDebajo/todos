package ru.debajo.todos.data.db

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import ru.debajo.todos.data.db.dao.InMemoryTable
import ru.debajo.todos.data.db.model.DbTodoGroup
import ru.debajo.todos.data.db.model.DbTodoGroupToItemLink
import ru.debajo.todos.data.db.model.DbTodoItem
import ru.debajo.todos.data.storage.model.StorageFile

class FileSession {

    private val onUpdateListeners: MutableSet<suspend () -> Unit> = mutableSetOf()
    private val onOpenListeners: MutableSet<suspend (StorageFile) -> Unit> = mutableSetOf()
    private val onCloseListeners: MutableSet<suspend (StorageFile) -> Unit> = mutableSetOf()

    private val currentFileFlow: MutableStateFlow<StorageFile?> = MutableStateFlow(null)
    private val currentFileEdited: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val edited: StateFlow<Boolean> = currentFileEdited.asStateFlow()

    val isOpened: Boolean
        inline get() = currentFile != null

    val currentFile: StorageFile?
        get() = currentFileFlow.value

    private var dbTodoGroupTableInternal: InMemoryTable<DbTodoGroup>? = null
    private var dbTodoGroupToItemLinkTableInternal: InMemoryTable<DbTodoGroupToItemLink>? = null
    private var dbTodoItemTableInternal: InMemoryTable<DbTodoItem>? = null

    val dbTodoGroupTable: InMemoryTable<DbTodoGroup>
        get() = requireNotNull(dbTodoGroupTableInternal)
    val dbTodoGroupToItemLinkTable: InMemoryTable<DbTodoGroupToItemLink>
        get() = requireNotNull(dbTodoGroupToItemLinkTableInternal)
    val dbTodoItemTable: InMemoryTable<DbTodoItem>
        get() = requireNotNull(dbTodoItemTableInternal)

    private var notifyUpdate: Boolean = true

    suspend fun disableNotifyUpdate(block: suspend () -> Unit) {
        notifyUpdate = false
        try {
            block()
        } finally {
            notifyUpdate = true
        }
    }

    suspend fun awaitOpened(): StorageFile {
        return currentFileFlow.filterNotNull().first()
    }

    suspend fun open(file: StorageFile) {
        if (this.currentFile == file) {
            return
        }

        close()
        currentFileFlow.value = file
        dbTodoGroupTableInternal = InMemoryTable(onUpdate = ::onUpdate)
        dbTodoGroupToItemLinkTableInternal = InMemoryTable(onUpdate = ::onUpdate)
        dbTodoItemTableInternal = InMemoryTable(onUpdate = ::onUpdate)
        onOpen(file)
    }

    suspend fun close() {
        val file = currentFile

        currentFileEdited.value = false
        currentFileFlow.value = null
        dbTodoGroupTableInternal = null
        dbTodoGroupToItemLinkTableInternal = null
        dbTodoItemTableInternal = null

        file?.let { onClose(it) }
    }

    fun addOnUpdateListener(listener: suspend () -> Unit): FileSession {
        onUpdateListeners.add(listener)
        return this
    }

    fun removeOnUpdateListener(listener: suspend () -> Unit) {
        onUpdateListeners.remove(listener)
    }

    fun addOnOpenListener(listener: suspend (StorageFile) -> Unit): FileSession {
        onOpenListeners.add(listener)
        return this
    }

    fun removeOnOpenListener(listener: suspend (StorageFile) -> Unit) {
        onOpenListeners.remove(listener)
    }

    fun addOnCloseListener(listener: suspend (StorageFile) -> Unit): FileSession {
        onCloseListeners.add(listener)
        return this
    }

    fun removeOnCloseListener(listener: suspend (StorageFile) -> Unit) {
        onCloseListeners.remove(listener)
    }

    private suspend fun onUpdate() {
        if (notifyUpdate) {
            currentFileEdited.value = true
            onUpdateListeners.forEach { it.invoke() }
        }
    }

    private suspend fun onOpen(file: StorageFile) {
        onOpenListeners.forEach { it.invoke(file) }
    }

    private suspend fun onClose(file: StorageFile) {
        onCloseListeners.forEach { it.invoke(file) }
    }
}
