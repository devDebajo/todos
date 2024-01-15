package ru.debajo.todos.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ru.debajo.todos.common.swapLeft
import ru.debajo.todos.common.swapRight
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupQueries
import ru.debajo.todos.di.AsyncProvider

class DbTodoGroupDao(
    private val queriesProvider: AsyncProvider<DbTodoGroupQueries>,
) {
    fun observeGroups(): Flow<List<DbTodoGroup>> {
        return flow {
            emitAll(
                queriesProvider.provide().getAll().asFlow().mapToList(Dispatchers.IO)
            )
        }
    }

    suspend fun getAll(): List<DbTodoGroup> {
        return withContext(Dispatchers.IO) {
            queriesProvider.provide().getAll().executeAsList()
        }
    }

    suspend fun save(id: String, name: String) {
        withContext(Dispatchers.IO) {
            val queries = queriesProvider.provide()
            queries.transaction {
                val count = queries.count().executeAsOne()
                queries.save(id, name, count)
            }
        }
    }

    suspend fun rename(groupId: String, name: String) {
        withContext(Dispatchers.IO) {
            queriesProvider.provide().rename(id = groupId, name = name)
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            queriesProvider.provide().delete(id)
        }
    }

    suspend fun updateOrder(id: String, moveRight: Boolean) {
        withContext(Dispatchers.IO) {
            val queries = queriesProvider.provide()
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
