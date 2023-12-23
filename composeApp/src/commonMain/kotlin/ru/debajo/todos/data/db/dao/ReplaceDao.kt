package ru.debajo.todos.data.db.dao

import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.db.DbTodoItem

// TODO implement
class ReplaceDao {
    suspend fun replace(
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        items: List<DbTodoItem>,
    ) {

    }
}
