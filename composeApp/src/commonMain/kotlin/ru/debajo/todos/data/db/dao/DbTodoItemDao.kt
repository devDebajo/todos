package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbTodoItem
import ru.debajo.todos.db.DbTodoItemQueries

class DbTodoItemDao(private val queries: DbTodoItemQueries) {
    suspend fun save(item: DbTodoItem) {
        withContext(IO) {
            queries.transaction {
                queries.delete(item.id)
                queries.save(
                    id = item.id,
                    text = item.text,
                    createTimestamp = item.createTimestamp,
                    updateTimestamp = item.updateTimestamp,
                    done = item.done,
                )
            }
        }
    }

    suspend fun get(id: String): DbTodoItem? {
        return withContext(IO) {
            queries.get(id).executeAsOneOrNull()
        }
    }

    suspend fun delete(id: String) {
        withContext(IO) {
            queries.delete(id)
        }
    }

    suspend fun delete(ids: List<String>) {
        withContext(IO) {
            queries.deleteByIds(ids)
        }
    }

    fun observeAll(): Flow<List<DbTodoItem>> = queries.getAll().asFlow().mapToList(IO)

    suspend fun getAll(): List<DbTodoItem> {
        return withContext(IO) {
            queries.getAll().executeAsList()
        }
    }
}
