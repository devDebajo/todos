package ru.debajo.todos.data.db.dao

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import ru.debajo.todos.db.DbFilePathQueries

class DbFilePathDao(private val queries: DbFilePathQueries) {

    suspend fun save(path: String) {
        withContext(IO) {
            queries.transaction {
                queries.clear()
                queries.save(path)
            }
        }
    }

    suspend fun get(): String? {
        return withContext(IO) {
            queries.get().executeAsOneOrNull()
        }
    }
}
