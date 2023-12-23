package ru.debajo.todos.data.db

import app.cash.sqldelight.db.SqlDriver

fun interface DriverFactory {
    fun createDriver(): SqlDriver
}

expect fun createSchema(driver: SqlDriver)
