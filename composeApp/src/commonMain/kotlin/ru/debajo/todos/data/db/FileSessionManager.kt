package ru.debajo.todos.data.db

import ru.debajo.todos.data.db.model.DbTodoGroup
import ru.debajo.todos.data.db.model.DbTodoGroupToItemLink
import ru.debajo.todos.data.db.model.DbTodoItem
import ru.debajo.todos.data.storage.FileHelper
import ru.debajo.todos.data.storage.model.StorageFile

class FileSessionManager(
    val fileSession: FileSession,
    private val fileHelper: FileHelper,
) {
    suspend fun open(
        path: String,
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {
        open(
            file = fileHelper.createStorageFile(path) ?: return,
            groups = groups,
            links = links,
            items = items,
        )
    }

    suspend fun open(
        file: StorageFile,
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {
        fileSession.open(file)
        fileSession.dbTodoGroupTable.updateRaw { groups.toList() }
        fileSession.dbTodoGroupToItemLinkTable.updateRaw { links.toList() }
        fileSession.dbTodoItemTable.updateRaw { items.toList() }
    }

    suspend fun close() {
        fileSession.close()
    }
}
