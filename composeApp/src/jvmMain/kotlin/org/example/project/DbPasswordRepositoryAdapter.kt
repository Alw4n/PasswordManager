package org.example.project

import org.example.project.db.AuthManager
import org.example.project.db.PasswordRepository as DbPassRepo
import org.example.project.db.WrapAuth
import org.example.project.db.SettingsRepository
import kotlin.collections.emptyList

/**
 * Адаптер между старым интерфейсом org.example.project.PasswordRepository
 * и новой DB-реализацией (org.example.project.db.PasswordRepository).
 *
 * Преобразует типы PasswordEntry и реализует методы аутентификации через AuthManager/WrapAuth.
 */
class DbPasswordRepositoryAdapter : PasswordRepository {
    // --- auth-like methods ------------------------------------------------
    override fun hasMasterPassword(): Boolean {
        // считаем, что "запомненный" мастер хранится через WrapAuth/Settings
        return AuthManager.hasRemembered()
    }

    override fun setMasterPassword(masterPassword: String): Boolean {
        // создавать "запомненный" мастер-ключ (для простоты)
        return try {
            // WrapAuth.setRememberedMaster ожидает CharArray
            WrapAuth.setRememberedMaster(masterPassword.toCharArray())
            true
        } catch (_: Throwable) {
            false
        }
    }

    override fun open(masterPassword: String): Boolean {
        return AuthManager.tryUnlock(masterPassword.toCharArray())
    }

    override fun close() {
        AuthManager.lock()
    }

    override fun changeMasterPassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            AuthManager.changePassword(oldPassword.toCharArray(), newPassword.toCharArray())
        } catch (_: Throwable) {
            false
        }
    }

    // --- password CRUD ----------------------------------------------------
    override fun list(): List<PasswordEntry> {
        return try {
            val dbList = DbPassRepo.listEntriesMeta()
            dbList.map { db ->
                PasswordEntry(
                    id = db.id.toString(),
                    title = db.title,
                    login = db.username ?: "",
                    secret = "<redacted>", // secrets stored encrypted in DB; don't expose
                    notes = db.notes ?: "",
                    tags = emptyList(),
                    createdAt = db.createdAtEpochMs,
                    updatedAt = db.updatedAtEpochMs
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override fun upsert(entry: PasswordEntry) {
        try {
            // if id parses to Long -> update, else create
            val idLong = entry.id.toLongOrNull()
            if (idLong != null) {
                // updateEntry signature: updateEntry(id: Long, title: String? = null, username: String? = null, passwordPlain: String? = null, notes: String? = null)
                // we don't have plain secret here — treat secret field as plaintext only if it's not the redacted marker
                val pwPlain = if (entry.secret != "<redacted>") entry.secret else null
                DbPassRepo.updateEntry(
                    id = idLong,
                    title = entry.title,
                    username = if (entry.login.isBlank()) null else entry.login,
                    passwordPlain = pwPlain,
                    notes = if (entry.notes.isBlank()) null else entry.notes
                )
            } else {
                // create new entry; use secret as plaintext
                DbPassRepo.createEntry(
                    title = entry.title,
                    username = if (entry.login.isBlank()) null else entry.login,
                    passwordPlain = entry.secret,
                    notes = if (entry.notes.isBlank()) null else entry.notes
                )
            }
        } catch (_: Throwable) {
            // swallow — UI should handle refresh/errors via repository usage
        }
    }

    override fun delete(id: String) {
        val idLong = id.toLongOrNull() ?: return
        try {
            DbPassRepo.deleteEntry(idLong)
        } catch (_: Throwable) {}
    }

    // legacy close()/open already implemented above
}
