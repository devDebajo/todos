package ru.debajo.todos.data.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import ru.debajo.todos.security.AesHelper
import ru.debajo.todos.security.decryptString
import ru.debajo.todos.security.encryptString

class EncryptedSqlDriver(
    private val delegate: SqlDriver,
    private val secret: String,
) : SqlDriver by delegate {
    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        return delegate.execute(
            identifier = identifier,
            sql = sql,
            parameters = parameters,
            binders = if (binders == null) {
                null
            } else {
                { EncryptedSqlPreparedStatement(this, secret).binders() }
            }
        )
    }

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        return delegate.executeQuery(
            identifier = identifier,
            sql = sql,
            mapper = { cursor -> mapper(EncryptedSqlCursor(cursor, secret)) },
            parameters = parameters,
            binders = if (binders == null) {
                null
            } else {
                { EncryptedSqlPreparedStatement(this, secret).binders() }
            }
        )
    }
}

private class EncryptedSqlCursor(
    private val delegate: SqlCursor,
    private val secret: String,
) : SqlCursor by delegate {
    override fun getString(index: Int): String? {
        val encryptedString = delegate.getString(index) ?: return null
        return AesHelper.decryptString(secret, encryptedString)
    }

    override fun getBoolean(index: Int): Boolean? = getString(index)?.toBooleanStrict()

    override fun getBytes(index: Int): ByteArray? {
        val encryptedBytes = delegate.getBytes(index) ?: return null
        return AesHelper.decryptBytes(secret, encryptedBytes)
    }

    override fun getLong(index: Int): Long? = getString(index)?.toLong()

    override fun getDouble(index: Int): Double? {
        val bits = getLong(index) ?: return null
        return Double.fromBits(bits)
    }
}

private class EncryptedSqlPreparedStatement(
    private val delegate: SqlPreparedStatement,
    private val secret: String,
) : SqlPreparedStatement by delegate {
    override fun bindString(index: Int, string: String?) {
        delegate.bindString(
            index = index,
            string = string?.let { AesHelper.encryptString(secret, it) },
        )
    }

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        delegate.bindBytes(
            index = index,
            bytes = bytes?.let { AesHelper.encryptBytes(secret, it) },
        )
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        bindString(index, boolean?.toString())
    }

    override fun bindLong(index: Int, long: Long?) {
        bindString(index, long?.toString())
    }

    override fun bindDouble(index: Int, double: Double?) {
        bindLong(index, double?.toBits())
    }
}
