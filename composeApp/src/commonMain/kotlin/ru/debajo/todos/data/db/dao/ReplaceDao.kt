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
import ru.debajo.todos.di.AsyncProvider

class ReplaceDao(
    private val todosDatabaseProvider: AsyncProvider<TodosDatabase>,
    private val dbTodoGroupQueriesProvider: AsyncProvider<DbTodoGroupQueries>,
    private val dbTodoItemQueriesProvider: AsyncProvider<DbTodoItemQueries>,
    private val dbTodoGroupToItemLinkQueriesProvider: AsyncProvider<DbTodoGroupToItemLinkQueries>,
    private val dbFilePathQueriesProvider: AsyncProvider<DbFilePathQueries>,
) {
    suspend fun replace(
        path: String,
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {
        withContext(IO) {
            val dbTodoGroupQueries = dbTodoGroupQueriesProvider.provide()
            val dbTodoItemQueries = dbTodoItemQueriesProvider.provide()
            val dbTodoGroupToItemLinkQueries = dbTodoGroupToItemLinkQueriesProvider.provide()
            val dbFilePathQueries = dbFilePathQueriesProvider.provide()
            todosDatabaseProvider.provide().transaction {
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
