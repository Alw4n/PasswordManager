package org.example.project.db

import org.jetbrains.exposed.dao.id.LongIdTable

object Passwords : LongIdTable("passwords") {
    val title = varchar("title", 255)
    val username = varchar("username", 255).nullable()
    val passwordCipher = text("password_cipher") // base64(iv + ciphertext)
    val notes = text("notes").nullable()
    val createdAt = long("created_at") // epoch millis
    val updatedAt = long("updated_at") // epoch millis
}
