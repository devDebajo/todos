package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbFilePathQueries
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupQueries
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.db.DbTodoGroupToItemLinkQueries
import ru.debajo.todos.db.DbTodoItem
import ru.debajo.todos.db.DbTodoItemQueries
import ru.debajo.todos.db.TodosDatabase

class ReplaceDao(
    private val todosDatabase: TodosDatabase,
    private val dbTodoGroupQueries: DbTodoGroupQueries,
    private val dbTodoItemQueries: DbTodoItemQueries,
    private val dbTodoGroupToItemLinkQueries: DbTodoGroupToItemLinkQueries,
    private val dbFilePathQueries: DbFilePathQueries,
) {
    suspend fun replace(
        path: String,
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {
        withContext(IO) {
            todosDatabase.transaction {
                dbTodoGroupQueries.deleteAll()
                dbTodoItemQueries.deleteAll()
                dbTodoGroupToItemLinkQueries.deleteAll()
                dbFilePathQueries.clear()
                dbFilePathQueries.save(path)

                for (group in groups) {
                    dbTodoGroupQueries.save(
                        id = group.id,
                        name = group.name,
                        position = group.position,
                    )
                }

                for (link in links) {
                    dbTodoGroupToItemLinkQueries.save(
                        groupId = link.groupId,
                        todoId = link.todoId
                    )
                }

                for (item in items) {
                    dbTodoItemQueries.save(
                        id = item.id,
                        text = item.text,
                        createTimestamp = item.createTimestamp,
                        updateTimestamp = item.updateTimestamp,
                        done = item.done,
                    )
                }
            }
        }
    }
}
