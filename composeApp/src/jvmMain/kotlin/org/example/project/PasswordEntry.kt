package org.example.project

import java.util.UUID

data class PasswordEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val login: String,
    val secret: String,
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
