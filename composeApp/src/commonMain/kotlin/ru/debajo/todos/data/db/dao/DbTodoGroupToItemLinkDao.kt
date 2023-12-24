package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.db.DbTodoGroupToItemLinkQueries

class DbTodoGroupToItemLinkDao(private val queries: DbTodoGroupToItemLinkQueries) {
    suspend fun save(link: DbTodoGroupToItemLink) {
        withContext(IO) {
            queries.transaction {
                queries.deleteByTodoId(todoId = link.todoId)
                queries.save(groupId = link.groupId, todoId = link.todoId)
            }
        }
    }

    suspend fun deleteByGroup(groupId: String) {
        withContext(IO) {
            queries.deleteByGroupId(groupId = groupId)
        }
    }

    suspend fun deleteByTodoId(todoId: String) {
        withContext(IO) {
            queries.deleteByTodoId(todoId = todoId)
        }
    }

    suspend fun deleteByTodoIds(todoIds: List<String>) {
        withContext(IO) {
            queries.deleteByTodoIds(todoIds)
        }
    }

    suspend fun delete(groupId: String, todoId: String) {
        withContext(IO) {
            queries.delete(groupId = groupId, todoId = todoId)
        }
    }

    suspend fun getByGroupId(groupId: String): List<DbTodoGroupToItemLink> {
        return withContext(IO) {
            queries.getByGroupId(groupId = groupId).executeAsList()
        }
    }

    suspend fun getAll(): List<DbTodoGroupToItemLink> {
        return withContext(IO) {
            queries.getAll().executeAsList()
        }
    }

    fun observe(): Flow<List<DbTodoGroupToItemLink>> = queries.getAll().asFlow().mapToList(IO)
}
