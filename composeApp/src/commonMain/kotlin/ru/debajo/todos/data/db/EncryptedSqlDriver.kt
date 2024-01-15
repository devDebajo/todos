package ru.debajo.todos.data.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import ru.debajo.todos.common.createString
import ru.debajo.todos.common.toBooleanStrict
import ru.debajo.todos.common.toByteArray
import ru.debajo.todos.common.toLong
import ru.debajo.todos.security.AesHelper

class EncryptedSqlDriver(
    private val delegate: SqlDriver,
    private val secret: String,
) : SqlDriver by delegate {

    private val bytesDecryptor: (ByteArray) -> ByteArray = { AesHelper.decryptBytes(secret, it) }
    private val bytesEncryptor: (ByteArray) -> ByteArray = { AesHelper.encryptBytes(secret, it) }

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
                { EncryptedSqlPreparedStatement(this, bytesEncryptor).binders() }
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
            mapper = { cursor -> mapper(EncryptedSqlCursor(cursor, bytesDecryptor)) },
            parameters = parameters,
            binders = if (binders == null) {
                null
            } else {
                { EncryptedSqlPreparedStatement(this, bytesEncryptor).binders() }
            }
        )
    }
}

private class EncryptedSqlCursor(
    private val delegate: SqlCursor,
    private val bytesDecryptor: (ByteArray) -> ByteArray,
) : SqlCursor by delegate {

    override fun getBytes(index: Int): ByteArray? {
        val encryptedBytes = delegate.getBytes(index) ?: return null
        return bytesDecryptor(encryptedBytes)
    }

    override fun getString(index: Int): String? {
        val stringBytes = getBytes(index) ?: return null
        return stringBytes.createString()
    }

    override fun getLong(index: Int): Long? {
        return try {
            getString(index)?.toLong()
        } catch (e: Throwable) {
            delegate.getLong(index)
        }
    }

    override fun getBoolean(index: Int): Boolean? = getLong(index)?.toBooleanStrict()

    override fun getDouble(index: Int): Double? = getLong(index)?.let { Double.fromBits(it) }
}

private class EncryptedSqlPreparedStatement(
    private val delegate: SqlPreparedStatement,
    private val bytesEncryptor: (ByteArray) -> ByteArray,
) : SqlPreparedStatement by delegate {

    override fun bindBytes(index: Int, bytes: ByteArray?) {
        delegate.bindBytes(index, bytes?.let(bytesEncryptor))
    }

    override fun bindString(index: Int, string: String?) {
        bindBytes(index, string?.toByteArray())
    }

    override fun bindLong(index: Int, long: Long?) {
        bindString(index, long?.toString())
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        bindLong(index, boolean?.toLong())
    }

    override fun bindDouble(index: Int, double: Double?) {
        bindLong(index, double?.toBits())
    }
}
