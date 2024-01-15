package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbFilePathQueries
import ru.debajo.todos.di.AsyncProvider

class DbFilePathDao(
    private val queriesProvider: AsyncProvider<DbFilePathQueries>,
) {

    suspend fun save(path: String) {
        withContext(Dispatchers.IO) {
            val queries = queriesProvider.provide()
            queries.transaction {
                queries.clear()
                queries.save(path)
            }
        }
    }

    suspend fun get(): String? {
        return withContext(Dispatchers.IO) {
            queriesProvider.provide().get().executeAsOneOrNull()
        }
    }
}
