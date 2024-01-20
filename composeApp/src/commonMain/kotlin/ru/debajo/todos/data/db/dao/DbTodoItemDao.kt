package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.flow.Flow
import ru.debajo.todos.common.UUID
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.db.model.DbTodoItem

class DbTodoItemDao(fileSession: FileSession) {

    private val table: InMemoryTable<DbTodoItem> by fileSession::dbTodoItemTable

    suspend fun save(item: DbTodoItem) {
        table.insertOrReplace(item) { it.id.toString() }
    }

    suspend fun get(id: UUID): DbTodoItem? {
        return table.firstOrNull { it.id == id }
    }

    suspend fun delete(id: UUID) {
        table.deleteBy { it.id == id }
    }

    suspend fun delete(ids: List<UUID>) {
        val idsSet = ids.toSet()
        table.deleteBy { it.id in idsSet }
    }

    fun observeAll(): Flow<List<DbTodoItem>> = table.observe()

    suspend fun getAll(): List<DbTodoItem> = table.getAll()
}
