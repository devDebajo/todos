package ru.debajo.todos.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.debajo.todos.common.UUID
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.storage.DatabaseChangeListener
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupToItemLink
import ru.debajo.todos.strings.R

class TodoGroupRepository(
    private val dbTodoGroupDao: DbTodoGroupDao,
    private val dbTodoGroupToItemLinkDao: DbTodoGroupToItemLinkDao,
    private val todoItemRepository: TodoItemRepository,
    private val databaseChangeListener: DatabaseChangeListener,
) {
    fun observe(): Flow<List<TodoGroup>> {
        return combine(
            dbTodoGroupDao.observeGroups(),
            dbTodoGroupToItemLinkDao.observe(),
            todoItemRepository.observe()
        ) { groups, links, todos ->
            prepare(groups, links, todos)
        }
    }

    suspend fun createGroup(name: String): TodoGroup {
        val id = UUID.randomUUID().toString()
        dbTodoGroupDao.save(id, name)
        databaseChangeListener.onUpdate()
        return TodoGroup(
            id = GroupId(id),
            name = name,
            actualTodos = emptyList(),
            doneTodos = emptyList(),
            editable = true,
        )
    }

    suspend fun deleteGroup(id: GroupId, withTodos: Boolean) {
        dbTodoGroupDao.delete(id.id)
        if (withTodos) {
            val links = dbTodoGroupToItemLinkDao.getByGroupId(id.id).map { TodoId(it.todoId) }
            todoItemRepository.delete(links)
        }
        dbTodoGroupToItemLinkDao.deleteByGroup(id.id)
        databaseChangeListener.onUpdate()
    }

    suspend fun renameGroup(groupId: GroupId, name: String) {
        dbTodoGroupDao.rename(groupId.id, name)
        databaseChangeListener.onUpdate()
    }

    suspend fun link(groupId: GroupId, todoId: TodoId) {
        dbTodoGroupToItemLinkDao.save(DbTodoGroupToItemLink(groupId.id, todoId.id))
        databaseChangeListener.onUpdate()
    }

    suspend fun moveLeft(groupId: GroupId) {
        dbTodoGroupDao.updateOrder(groupId.id, moveRight = false)
        databaseChangeListener.onUpdate()
    }

    suspend fun moveRight(groupId: GroupId) {
        dbTodoGroupDao.updateOrder(groupId.id, moveRight = true)
        databaseChangeListener.onUpdate()
    }

    private fun prepare(
        groups: List<DbTodoGroup>,
        links: List<DbTodoGroupToItemLink>,
        todos: List<TodoItem>,
    ): List<TodoGroup> {
        val unlinkedTodos = todos.toMutableSet()
        val groupsMapping = links.groupBy(
            keySelector = { it.groupId },
            valueTransform = { it.todoId }
        )
        val result = mutableListOf<TodoGroup>()
        for (group in groups) {
            val todosIds = groupsMapping[group.id].orEmpty().toHashSet()
            val groupTodos = todos.filter { it.id.id in todosIds }
            for (groupTodo in groupTodos) {
                unlinkedTodos.remove(groupTodo)
            }
            result.add(createGroup(GroupId(group.id), group.name, groupTodos))
        }
        result.add(createAllGroup(todos))
        if (unlinkedTodos.isNotEmpty() && unlinkedTodos.size != todos.size) {
            result.add(createOtherGroup(unlinkedTodos.toList()))
        }
        return result
    }

    private fun createAllGroup(todos: List<TodoItem>): TodoGroup {
        return createGroup(
            id = AllTodosGroupId,
            name = R.strings.allTodosGroupName,
            todos = todos,
            editable = false,
        )
    }

    private fun createOtherGroup(todos: List<TodoItem>): TodoGroup {
        return createGroup(
            id = OtherTodosGroupId,
            name = R.strings.otherTodosGroupName,
            todos = todos,
            editable = false,
        )
    }

    private fun createGroup(
        id: GroupId,
        name: String,
        todos: List<TodoItem>,
        editable: Boolean = true,
    ): TodoGroup {
        val (done, actual) = todos.partition { it.done }
        return TodoGroup(
            id = id,
            name = name,
            editable = editable,
            doneTodos = done.sortedByDescending { it.updateTimestamp },
            actualTodos = actual.sortedByDescending { it.updateTimestamp },
        )
    }
}
