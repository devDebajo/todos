package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbTodoItem
import ru.debajo.todos.db.DbTodoItemQueries
import ru.debajo.todos.di.AsyncProvider

class DbTodoItemDao(
    private val queriesProvider: AsyncProvider<DbTodoItemQueries>,
) {
    suspend fun save(item: DbTodoItem) {
        withContext(IO) {
            val queries = queriesProvider.provide()
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
            queriesProvider.provide().get(id).executeAsOneOrNull()
        }
    }

    suspend fun delete(id: String) {
        withContext(IO) {
            queriesProvider.provide().delete(id)
        }
    }

    suspend fun delete(ids: List<String>) {
        withContext(IO) {
            queriesProvider.provide().deleteByIds(ids)
        }
    }

    fun observeAll(): Flow<List<DbTodoItem>> {
        return flow {
            emitAll(
                queriesProvider.provide().getAll().asFlow().mapToList(IO)
            )
        }
    }

    suspend fun getAll(): List<DbTodoItem> {
        return withContext(IO) {
            queriesProvider.provide().getAll().executeAsList()
        }
    }
}
