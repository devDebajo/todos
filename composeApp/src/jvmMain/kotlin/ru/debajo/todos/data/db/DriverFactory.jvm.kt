package ru.debajo.todos.data.db

import app.cash.sqldelight.db.SqlDriver
import java.io.File
import ru.debajo.todos.db.TodosDatabase

internal const val DatabaseFilePath: String = "todos.db"

actual fun createSchema(driver: SqlDriver) {
    TodosDatabase.Schema.create(driver)
}

actual fun deleteDatabaseFile() {
    File(DatabaseFilePath).delete()
}
