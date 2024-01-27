package ru.debajo.todos.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import ru.debajo.todos.common.UUID
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.db.dao.DbTodoItemDao
import ru.debajo.todos.data.db.model.DbTodoItem

class TodoItemRepository(
    private val dbTodoItemDao: DbTodoItemDao,
    private val dbTodoGroupToItemLinkDao: DbTodoGroupToItemLinkDao,
) {

    fun observe(): Flow<List<TodoItem>> {
        return dbTodoItemDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun create(id: UUID, text: String): TodoItem {
        val dbItem = createDbTodoItem(id = id, text = text)
        dbTodoItemDao.save(dbItem)
        return dbItem.toDomain()
    }

    suspend fun update(id: TodoId, text: String): TodoItem {
        val dbItem = dbTodoItemDao.get(id.id)?.copy(
            text = text,
            updateTimestamp = Clock.System.now(),
        ) ?: createDbTodoItem(id = id.id, text = text)

        dbTodoItemDao.save(dbItem)
        return dbItem.toDomain()
    }

    suspend fun delete(id: TodoId) {
        dbTodoItemDao.delete(id.id)
        dbTodoGroupToItemLinkDao.deleteByTodoId(id.id)
    }

    suspend fun delete(ids: List<TodoId>) {
        dbTodoItemDao.delete(ids.map { it.id })
        dbTodoGroupToItemLinkDao.deleteByTodoIds(ids.map { it.id })
    }

    suspend fun updateDone(id: TodoId, done: Boolean) {
        val dbItem = dbTodoItemDao.get(id.id) ?: return
        dbTodoItemDao.save(
            dbItem.copy(
                done = done,
                updateTimestamp = Clock.System.now(),
            )
        )
    }

    private fun createDbTodoItem(id: UUID, text: String): DbTodoItem {
        val now = Clock.System.now()
        return DbTodoItem(
            id = id,
            text = text,
            createTimestamp = now,
            updateTimestamp = now,
            done = false,
        )
    }

    private fun DbTodoItem.toDomain(): TodoItem {
        return TodoItem(
            id = TodoId(id),
            text = text,
            createTimestamp = createTimestamp,
            updateTimestamp = updateTimestamp,
            done = done,
        )
    }
}
