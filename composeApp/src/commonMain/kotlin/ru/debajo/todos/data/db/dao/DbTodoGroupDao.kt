package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.common.swapLeft
import ru.debajo.todos.common.swapRight
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupQueries

class DbTodoGroupDao(private val queries: DbTodoGroupQueries) {

    fun observeGroups(): Flow<List<DbTodoGroup>> = queries.getAll().asFlow().mapToList(IO)

    suspend fun getAll(): List<DbTodoGroup> {
        return withContext(IO) {
            queries.getAll().executeAsList()
        }
    }

    suspend fun save(id: String, name: String) {
        withContext(IO) {
            queries.transaction {
                val count = queries.count().executeAsOne()
                queries.save(id, name, count)
            }
        }
    }

    suspend fun rename(groupId: String, name: String) {
        withContext(IO) {
            queries.rename(id = groupId, name = name)
        }
    }

    suspend fun delete(id: String) {
        withContext(IO) {
            queries.delete(id)
        }
    }

    suspend fun updateOrder(id: String, moveRight: Boolean) {
        withContext(IO) {
            queries.transaction {
                val mutableGroups = queries.getAll().executeAsList().toMutableList()
                val index = mutableGroups.indexOfFirst { it.id == id }
                if (index >= 0) {
                    if (moveRight) {
                        mutableGroups.swapRight(index)
                    } else {
                        mutableGroups.swapLeft(index)
                    }

                    for ((groupIndex, group) in mutableGroups.withIndex()) {
                        queries.changePosition(
                            id = group.id,
                            position = groupIndex.toLong(),
                        )
                    }
                }
            }
        }
    }
}
