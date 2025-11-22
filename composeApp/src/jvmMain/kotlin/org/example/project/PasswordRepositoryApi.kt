package org.example.project

interface PasswordRepository {
    fun hasMasterPassword(): Boolean
    fun setMasterPassword(masterPassword: String): Boolean
    fun open(masterPassword: String): Boolean
    fun list(): List<PasswordEntry>
    fun upsert(entry: PasswordEntry)
    fun delete(id: String)
    fun close()
    fun changeMasterPassword(oldPassword: String, newPassword: String): Boolean
}
