package ru.debajo.todos.data.db

import ru.debajo.todos.data.db.model.DbTodoGroup
import ru.debajo.todos.data.db.model.DbTodoGroupToItemLink
import ru.debajo.todos.data.db.model.DbTodoItem

class FileSessionManager(
    val fileSession: FileSession,
) {
    suspend fun fill(
        path: String,
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {
        if (fileSession.currentFile?.absolutePath != path) {
            throw IllegalStateException("File not opened or different")
        }
        fileSession.dbTodoGroupTable.updateRaw { groups.toList() }
        fileSession.dbTodoGroupToItemLinkTable.updateRaw { links.toList() }
        fileSession.dbTodoItemTable.updateRaw { items.toList() }
    }

    suspend fun close() {
        fileSession.close()
    }
}
