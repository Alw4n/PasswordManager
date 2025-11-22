package org.example.project.db

import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class PasswordEntry(
    val id: Long,
    val title: String,
    val username: String?,
    val passwordPlain: String,
    val notes: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

object PasswordRepository {
    private val rng = SecureRandom()
    private const val IV_LEN = 12
    private const val GCM_TAG_BITS = 128
    private val b64 = Base64.getEncoder()

    private fun nowEpochMs(): Long = System.currentTimeMillis()

    private fun aesGcmEncrypt(plain: ByteArray, key: SecretKey): String {
        val iv = ByteArray(IV_LEN).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ct = cipher.doFinal(plain)
        val out = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ct, 0, out, iv.size, ct.size)
        return b64.encodeToString(out)
    }

    private fun aesGcmDecrypt(b64data: String, key: SecretKey): ByteArray {
        val all = Base64.getDecoder().decode(b64data)
        val iv = all.copyOfRange(0, IV_LEN)
        val ct = all.copyOfRange(IV_LEN, all.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ct)
    }

    fun createEntry(title: String, username: String?, passwordPlain: String, notes: String? = null): Long {
        val key = AuthManager.getMasterKey() ?: throw IllegalStateException("App locked: master key not available")
        val cipherText = aesGcmEncrypt(passwordPlain.toByteArray(Charsets.UTF_8), key)
        return transaction {
            val now = nowEpochMs()
            val id = Passwords.insertAndGetId {
                it[Passwords.title] = title
                it[Passwords.username] = username
                it[Passwords.passwordCipher] = cipherText
                it[Passwords.notes] = notes
                it[Passwords.createdAt] = now
                it[Passwords.updatedAt] = now
            }
            id.value
        }
    }

    fun getEntry(id: Long): PasswordEntry? = transaction {
        val row = Passwords.select { Passwords.id eq id }.firstOrNull() ?: return@transaction null
        val key = AuthManager.getMasterKey() ?: throw IllegalStateException("App locked: master key not available")
        val cipher = row[Passwords.passwordCipher]
        val plain = try {
            String(aesGcmDecrypt(cipher, key), Charsets.UTF_8)
        } catch (e: Exception) {
            "<decryption error>"
        }
        PasswordEntry(
            id = row[Passwords.id].value,
            title = row[Passwords.title],
            username = row[Passwords.username],
            passwordPlain = plain,
            notes = row[Passwords.notes],
            createdAtEpochMs = row[Passwords.createdAt],
            updatedAtEpochMs = row[Passwords.updatedAt]
        )
    }

    // list metadata only (no decryption), avoid using Query.limit() for compatibility
    fun listEntriesMeta(maxRows: Int = 100): List<PasswordEntry> = transaction {
        val seq = Passwords.selectAll()
        val out = ArrayList<PasswordEntry>(minOf(maxRows, 64))
        var count = 0
        for (row in seq) {
            if (count++ >= maxRows) break
            out += PasswordEntry(
                id = row[Passwords.id].value,
                title = row[Passwords.title],
                username = row[Passwords.username],
                passwordPlain = "<redacted>",
                notes = row[Passwords.notes],
                createdAtEpochMs = row[Passwords.createdAt],
                updatedAtEpochMs = row[Passwords.updatedAt]
            )
        }
        out
    }

    fun updateEntry(id: Long, title: String? = null, username: String? = null, passwordPlain: String? = null, notes: String? = null) {
        val key = if (passwordPlain != null) AuthManager.getMasterKey() ?: throw IllegalStateException("App locked: master key not available") else null
        val cipherText = passwordPlain?.let { aesGcmEncrypt(it.toByteArray(Charsets.UTF_8), key!!) }
        transaction {
            Passwords.update({ Passwords.id eq id }) { stmt ->
                if (title != null) stmt[Passwords.title] = title
                if (username != null) stmt[Passwords.username] = username
                if (cipherText != null) stmt[Passwords.passwordCipher] = cipherText
                if (notes != null) stmt[Passwords.notes] = notes
                stmt[Passwords.updatedAt] = nowEpochMs()
            }
        }
    }

    fun deleteEntry(id: Long) = transaction {
        Passwords.deleteWhere { Passwords.id eq id }
    }
}
