package org.example.project.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Db {
    fun init(path: String = "data.db") {
        Database.connect("jdbc:sqlite:$path", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(Passwords, Settings)
        }
    }
}
