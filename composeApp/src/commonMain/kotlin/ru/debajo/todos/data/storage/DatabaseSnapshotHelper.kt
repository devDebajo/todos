package ru.debajo.todos.data.storage

import kotlinx.datetime.Instant
import ru.debajo.todos.common.UUID
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.db.FileSessionManager
import ru.debajo.todos.data.db.model.DbTodoGroup
import ru.debajo.todos.data.db.model.DbTodoGroupToItemLink
import ru.debajo.todos.data.db.model.DbTodoItem
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageSnapshotWithMeta
import ru.debajo.todos.data.storage.model.StorageTodoGroup
import ru.debajo.todos.data.storage.model.StorageTodoGroupToItemLink
import ru.debajo.todos.data.storage.model.StorageTodoItem

class DatabaseSnapshotHelper(
    private val fileSessionManager: FileSessionManager,
) {
    private val fileSession: FileSession
        get() = fileSessionManager.fileSession

    suspend fun getSnapshot(timestamp: Instant, encrypted: Boolean): StorageSnapshotWithMeta {
        val currentFile = requireNotNull(fileSession.currentFile)
        return StorageSnapshotWithMeta(
            editTimestampUtc = timestamp.toEpochMilliseconds(),
            snapshot = StorageSnapshot(
                groups = fileSession.dbTodoGroupTable.getAll().map { it.convert() },
                links = fileSession.dbTodoGroupToItemLinkTable.getAll().map { it.convert() },
                todos = fileSession.dbTodoItemTable.getAll().map { it.convert() },
            ),
            absolutePath = currentFile.absolutePath,
            encrypted = encrypted,
        )
    }

    suspend fun replace(snapshot: StorageSnapshotWithMeta) {
        val path = snapshot.absolutePath
        if (path.isEmpty()) {
            error("Path should not be empty")
        }
        fileSessionManager.fill(
            path = path,
            groups = snapshot.snapshot.groups.map { it.convert() },
            links = snapshot.snapshot.links.map { it.convert() },
            items = snapshot.snapshot.todos.map { it.convert() },
        )
    }

    private fun DbTodoGroup.convert(): StorageTodoGroup {
        return StorageTodoGroup(
            id = id.toString(),
            name = name,
            order = position,
        )
    }

    private fun StorageTodoGroup.convert(): DbTodoGroup {
        return DbTodoGroup(
            id = UUID(id),
            name = name,
            position = order
        )
    }

    private fun DbTodoGroupToItemLink.convert(): StorageTodoGroupToItemLink {
        return StorageTodoGroupToItemLink(
            groupId = groupId.toString(),
            todoId = todoId.toString(),
        )
    }

    private fun StorageTodoGroupToItemLink.convert(): DbTodoGroupToItemLink {
        return DbTodoGroupToItemLink(
            groupId = UUID(groupId),
            todoId = UUID(todoId),
        )
    }

    private fun DbTodoItem.convert(): StorageTodoItem {
        return StorageTodoItem(
            id = id.toString(),
            text = text,
            createTimestamp = createTimestamp.toEpochMilliseconds(),
            updateTimestamp = updateTimestamp.toEpochMilliseconds(),
            done = done,
        )
    }

    private fun StorageTodoItem.convert(): DbTodoItem {
        return DbTodoItem(
            id = UUID(id),
            text = text,
            createTimestamp = Instant.fromEpochMilliseconds(createTimestamp),
            updateTimestamp = Instant.fromEpochMilliseconds(updateTimestamp),
            done = done,
        )
    }
}
