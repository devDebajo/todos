package ru.debajo.todos.domain

import kotlinx.coroutines.flow.Flow
import ru.debajo.todos.common.UUID

class TodoItemUseCase(
    private val todoItemRepository: TodoItemRepository,
    private val todoGroupRepository: TodoGroupRepository,
) {
    fun observeGroups(): Flow<List<TodoGroup>> = todoGroupRepository.observe()

    suspend fun createGroup(name: String): TodoGroup {
        return todoGroupRepository.createGroup(name)
    }

    suspend fun createTodo(text: String, groupId: GroupId): TodoItem {
        val id = UUID.randomUUID()
        if (!groupId.isSyntheticGroup()) {
            todoGroupRepository.link(groupId, TodoId(id))
        }
        return todoItemRepository.create(id, text)
    }

    suspend fun updateDone(todoId: TodoId, done: Boolean) {
        todoItemRepository.updateDone(todoId, done)
    }

    suspend fun updateTodo(todoId: TodoId, text: String) {
        todoItemRepository.update(todoId, text)
    }

    suspend fun delete(todoId: TodoId) {
        todoItemRepository.delete(todoId)
    }

    suspend fun deleteGroup(groupId: GroupId, withTodos: Boolean) {
        todoGroupRepository.deleteGroup(groupId, withTodos)
    }

    suspend fun renameGroup(groupId: GroupId, name: String) {
        todoGroupRepository.renameGroup(groupId, name)
    }

    suspend fun moveLeft(groupId: GroupId) {
        todoGroupRepository.moveLeft(groupId)
    }

    suspend fun moveRight(groupId: GroupId) {
        todoGroupRepository.moveRight(groupId)
    }
}
