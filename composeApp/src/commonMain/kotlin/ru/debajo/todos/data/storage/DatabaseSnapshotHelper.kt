package ru.debajo.todos.data.storage

import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.dao.ReplaceDao
import ru.debajo.todos.data.storage.model.StorageSnapshot
import ru.debajo.todos.data.storage.model.StorageTodoGroup
import ru.debajo.todos.data.storage.model.StorageTodoGroupToItemLink
import ru.debajo.todos.data.storage.model.StorageTodoItem
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.db.DbTodoItem

class DatabaseSnapshotHelper(
    private val dbTodoGroupDao: DbTodoGroupDao,
    private val dbTodoGroupToItemLinkDao: DbTodoGroupToItemLinkDao,
    private val dbTodoItemDao: DbTodoItemDao,
    private val replaceDao: ReplaceDao,
) {
    suspend fun getSnapshot(): StorageSnapshot {
        return StorageSnapshot(
            groups = dbTodoGroupDao.getAll().map { it.convert() },
            links = dbTodoGroupToItemLinkDao.getAll().map { it.convert() },
            todos = dbTodoItemDao.getAll().map { it.convert() }
        )
    }

    suspend fun replace(snapshot: StorageSnapshot) {
        replaceDao.replace(
            groups = snapshot.groups.map { it.convert() },
            links = snapshot.links.map { it.convert() },
            items = snapshot.todos.map { it.convert() },
        )
    }

    private fun DbTodoGroup.convert(): StorageTodoGroup {
        return StorageTodoGroup(
            id = id,
            name = name,
            order = position.toInt(),
        )
    }

    private fun StorageTodoGroup.convert(): DbTodoGroup {
        return DbTodoGroup(
            id = id,
            name = name,
            position = order.toLong()
        )
    }

    private fun DbTodoGroupToItemLink.convert(): StorageTodoGroupToItemLink {
        return StorageTodoGroupToItemLink(
            groupId = groupId,
            todoId = todoId,
        )
    }

    private fun StorageTodoGroupToItemLink.convert(): DbTodoGroupToItemLink {
        return DbTodoGroupToItemLink(
            groupId = groupId,
            todoId = todoId,
        )
    }

    private fun DbTodoItem.convert(): StorageTodoItem {
        return StorageTodoItem(
            id = id,
            text = text,
            createTimestamp = createTimestamp,
            updateTimestamp = updateTimestamp,
            done = done == 1L,
        )
    }

    private fun StorageTodoItem.convert(): DbTodoItem {
        return DbTodoItem(
            id = id,
            text = text,
            createTimestamp = createTimestamp,
            updateTimestamp = updateTimestamp,
            done = if (done) 1L else 0L,
        )
    }
}
