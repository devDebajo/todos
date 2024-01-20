package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.flow.Flow
import ru.debajo.todos.common.UUID
import ru.debajo.todos.common.swapLeft
import ru.debajo.todos.common.swapRight
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.db.model.DbTodoGroup

class DbTodoGroupDao(fileSession: FileSession) {

    private val table: InMemoryTable<DbTodoGroup> by fileSession::dbTodoGroupTable

    fun observeGroups(): Flow<List<DbTodoGroup>> = table.observe()

    suspend fun getAll(): List<DbTodoGroup> = table.getAll()

    suspend fun save(id: UUID, name: String) {
        table.updateRaw { groups ->
            groups + DbTodoGroup(id = id, name = name, position = groups.size)
        }
    }

    suspend fun rename(groupId: UUID, name: String) {
        table.updateBy(
            predicate = { it.id == groupId },
            updater = { it.copy(name = name) }
        )
    }

    suspend fun delete(id: UUID) {
        table.deleteBy { it.id == id }
    }

    suspend fun updateOrder(id: UUID, moveRight: Boolean) {
        table.updateRaw { groups ->
            val mutableGroups = groups.toMutableList()
            val index = mutableGroups.indexOfFirst { it.id == id }
            if (index >= 0) {
                if (moveRight) {
                    mutableGroups.swapRight(index)
                } else {
                    mutableGroups.swapLeft(index)
                }

                for ((groupIndex, group) in mutableGroups.withIndex()) {
                    val newGroup = group.copy(position = groupIndex)
                    mutableGroups.removeAt(groupIndex)
                    mutableGroups.add(groupIndex, newGroup)
                }
                mutableGroups
            } else {
                groups
            }
        }
    }
}
