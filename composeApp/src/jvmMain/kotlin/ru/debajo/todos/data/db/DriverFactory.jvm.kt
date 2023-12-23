package ru.debajo.todos.data.db

import app.cash.sqldelight.db.SqlDriver
import ru.debajo.todos.db.TodosDatabase

actual fun createSchema(driver: SqlDriver) {
    TodosDatabase.Schema.create(driver)
}
