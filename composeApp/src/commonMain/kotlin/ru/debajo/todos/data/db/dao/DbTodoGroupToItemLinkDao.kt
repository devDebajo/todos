package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.flow.Flow
import ru.debajo.todos.common.UUID
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.db.model.DbTodoGroupToItemLink

class DbTodoGroupToItemLinkDao(fileSession: FileSession) {

    private val table: InMemoryTable<DbTodoGroupToItemLink> by fileSession::dbTodoGroupToItemLinkTable

    suspend fun save(link: DbTodoGroupToItemLink) {
        table.insertOrReplace(link) { it.todoId.toString() }
    }

    suspend fun deleteByGroup(groupId: UUID) {
        table.deleteBy { it.groupId == groupId }
    }

    suspend fun deleteByTodoId(todoId: UUID) {
        table.deleteBy { it.todoId == todoId }
    }

    suspend fun deleteByTodoIds(todoIds: List<UUID>) {
        val todoIdsSet = todoIds.toSet()
        table.deleteBy { it.todoId in todoIdsSet }
    }

    suspend fun delete(groupId: UUID, todoId: UUID) {
        table.deleteBy { it.groupId == groupId && it.todoId == todoId }
    }

    suspend fun getByGroupId(groupId: UUID): List<DbTodoGroupToItemLink> {
        return table.filter { it.groupId == groupId }
    }

    suspend fun getAll(): List<DbTodoGroupToItemLink> = table.getAll()

    fun observe(): Flow<List<DbTodoGroupToItemLink>> = table.observe()
}
