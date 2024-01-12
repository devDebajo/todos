package ru.debajo.todos.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import ru.debajo.todos.di.getFromDi

internal const val DatabaseName: String = "todos.db"

actual fun createSchema(driver: SqlDriver) = Unit

actual fun deleteDatabaseFile() {
    getFromDi<Context>().deleteDatabase(DatabaseName)
}
