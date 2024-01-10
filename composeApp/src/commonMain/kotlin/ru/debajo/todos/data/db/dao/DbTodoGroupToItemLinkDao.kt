package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.db.DbTodoGroupToItemLinkQueries
import ru.debajo.todos.di.AsyncProvider

class DbTodoGroupToItemLinkDao(
    private val queriesProvider: AsyncProvider<DbTodoGroupToItemLinkQueries>,
) {
    suspend fun save(link: DbTodoGroupToItemLink) {
        withContext(IO) {
            val queries = queriesProvider.provide()
            queries.transaction {
                queries.deleteByTodoId(todoId = link.todoId)
                queries.save(groupId = link.groupId, todoId = link.todoId)
            }
        }
    }

    suspend fun deleteByGroup(groupId: String) {
        withContext(IO) {
            queriesProvider.provide().deleteByGroupId(groupId = groupId)
        }
    }

    suspend fun deleteByTodoId(todoId: String) {
        withContext(IO) {
            queriesProvider.provide().deleteByTodoId(todoId = todoId)
        }
    }

    suspend fun deleteByTodoIds(todoIds: List<String>) {
        withContext(IO) {
            queriesProvider.provide().deleteByTodoIds(todoIds)
        }
    }

    suspend fun delete(groupId: String, todoId: String) {
        withContext(IO) {
            queriesProvider.provide().delete(groupId = groupId, todoId = todoId)
        }
    }

    suspend fun getByGroupId(groupId: String): List<DbTodoGroupToItemLink> {
        return withContext(IO) {
            queriesProvider.provide().getByGroupId(groupId = groupId).executeAsList()
        }
    }

    suspend fun getAll(): List<DbTodoGroupToItemLink> {
        return withContext(IO) {
            queriesProvider.provide().getAll().executeAsList()
        }
    }

    fun observe(): Flow<List<DbTodoGroupToItemLink>> {
        return flow {
            emitAll(
                queriesProvider.provide().getAll().asFlow().mapToList(IO)
            )
        }
    }
}
