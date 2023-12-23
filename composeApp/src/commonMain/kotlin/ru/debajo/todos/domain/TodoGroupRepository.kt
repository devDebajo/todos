package ru.debajo.todos.domain

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.debajo.todos.data.db.dao.DbTodoGroupDao
import ru.debajo.todos.data.db.dao.DbTodoGroupToItemLinkDao
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.db.DbTodoGroup
import ru.debajo.todos.db.DbTodoGroupToItemLink

class TodoGroupRepository(
    private val dbTodoGroupDao: DbTodoGroupDao,
    private val dbTodoGroupToItemLinkDao: DbTodoGroupToItemLinkDao,
    private val todoItemRepository: TodoItemRepository,
    private val databaseSnapshotWorker: DatabaseSnapshotWorker,
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
        dbTodoGroupDao.save(DbTodoGroup(id, name))
        databaseSnapshotWorker.onUpdate()
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
        databaseSnapshotWorker.onUpdate()
    }

    suspend fun renameGroup(groupId: GroupId, name: String) {
        dbTodoGroupDao.save(DbTodoGroup(groupId.id, name))
        databaseSnapshotWorker.onUpdate()
    }

    suspend fun link(groupId: GroupId, todoId: TodoId) {
        dbTodoGroupToItemLinkDao.deleteByTodoId(todoId.id)
        dbTodoGroupToItemLinkDao.save(DbTodoGroupToItemLink(groupId.id, todoId.id))
        databaseSnapshotWorker.onUpdate()
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
        val result = mutableListOf(createAllGroup(todos))
        for (group in groups) {
            val todosIds = groupsMapping[group.id].orEmpty().toHashSet()
            val groupTodos = todos.filter { it.id.id in todosIds }
            for (groupTodo in groupTodos) {
                unlinkedTodos.remove(groupTodo)
            }
            result.add(createGroup(GroupId(group.id), group.name, groupTodos))
        }
        if (unlinkedTodos.isNotEmpty() && unlinkedTodos.size != todos.size) {
            result.add(createOtherGroup(unlinkedTodos.toList()))
        }
        return result
    }

    private fun createAllGroup(todos: List<TodoItem>): TodoGroup {
        return createGroup(
            id = AllTodosGroupId,
            name = AllTodosGroupName,
            todos = todos,
            editable = false,
        )
    }

    private fun createOtherGroup(todos: List<TodoItem>): TodoGroup {
        return createGroup(
            id = OtherTodosGroupId,
            name = "Other",
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