package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupQueries

class DbTodoGroupDao(private val queries: DbTodoGroupQueries) {

    fun observeGroups(): Flow<List<DbTodoGroup>> = queries.getAll().asFlow().mapToList(IO)

    suspend fun getAll(): List<DbTodoGroup> {
        return withContext(IO) {
            queries.getAll().executeAsList()
        }
    }

    suspend fun save(group: DbTodoGroup) {
        withContext(IO) {
            queries.save(group.id, group.name)
        }
    }

    suspend fun delete(id: String) {
        withContext(IO) {
            queries.delete(id)
        }
    }
}
