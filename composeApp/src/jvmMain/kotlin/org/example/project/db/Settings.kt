package org.example.project.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.associate

object Settings : Table("settings") {
    val key = varchar("key", 128)
    val value = text("value")

    // Создаём уникальный индекс на key вместо явного PrimaryKey — кросс‑версийный вариант
    init {
        index(true, key)
    }
}

object SettingsRepository {
    fun init() = transaction { SchemaUtils.create(Settings) }

    fun put(keyStr: String, valueStr: String) = transaction {
        val updated = Settings.update({ Settings.key eq keyStr }) { it[Settings.value] = valueStr }
        if (updated == 0) {
            Settings.insert { it[Settings.key] = keyStr; it[Settings.value] = valueStr }
        }
    }

    fun get(keyStr: String): String? = transaction {
        Settings.select { Settings.key eq keyStr }.map { row -> row[Settings.value] }.firstOrNull()
    }

    fun delete(keyStr: String) = transaction { Settings.deleteWhere { Settings.key eq keyStr } }

    fun getAll(): Map<String, String> = transaction {
        Settings.selectAll().associate { row -> row[Settings.key] to row[Settings.value] }
    }
}
