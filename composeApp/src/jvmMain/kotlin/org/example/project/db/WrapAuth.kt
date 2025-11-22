package org.example.project.db

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object WrapAuth {
    private val rng = SecureRandom()
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val KEY_BITS = 256
    private const val ITER = 200_000
    private const val AES_ALGO = "AES/GCM/NoPadding"
    private val json = Json { encodeDefaults = true }

    private fun newSalt() = ByteArray(SALT_LEN).also { rng.nextBytes(it) }
    private fun newIv() = ByteArray(IV_LEN).also { rng.nextBytes(it) }

    private fun pbkdf2(password: CharArray, salt: ByteArray, iter: Int = ITER): ByteArray {
        val spec = PBEKeySpec(password, salt, iter, KEY_BITS)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return skf.generateSecret(spec).encoded
    }

    private fun aesKeyFromBytes(b: ByteArray) = SecretKeySpec(b, "AES")

    // format stored: base64(salt(16) + iv(12) + ciphertext)
    private fun encode(b: ByteArray) = Base64.getEncoder().encodeToString(b)
    private fun decode(s: String) = Base64.getDecoder().decode(s)

    // wrap: encrypt masterKeyBytes with key derived from password
    fun wrapMaster(masterKeyBytes: ByteArray, password: CharArray): String {
        val salt = newSalt()
        val iv = newIv()
        val kek = aesKeyFromBytes(pbkdf2(password, salt))
        val cipher = Cipher.getInstance(AES_ALGO)
        cipher.init(Cipher.ENCRYPT_MODE, kek, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(masterKeyBytes)
        val out = ByteArray(salt.size + iv.size + ct.size)
        System.arraycopy(salt, 0, out, 0, salt.size)
        System.arraycopy(iv, 0, out, salt.size, iv.size)
        System.arraycopy(ct, 0, out, salt.size + iv.size, ct.size)
        return encode(out)
    }

    // unwrap: returns masterKeyBytes or null if password wrong
    fun unwrapMaster(wrappedBase64: String, password: CharArray): ByteArray? {
        return try {
            val all = decode(wrappedBase64)
            val salt = all.copyOfRange(0, SALT_LEN)
            val iv = all.copyOfRange(SALT_LEN, SALT_LEN + IV_LEN)
            val ct = all.copyOfRange(SALT_LEN + IV_LEN, all.size)
            val kek = aesKeyFromBytes(pbkdf2(password, salt))
            val cipher = Cipher.getInstance(AES_ALGO)
            cipher.init(Cipher.DECRYPT_MODE, kek, GCMParameterSpec(128, iv))
            cipher.doFinal(ct)
        } catch (e: Exception) {
            null
        }
    }

    // PUBLIC API keys in settings
    private const val SETTINGS_KEY = "auth.wrapped_master"

    // create and store wrapped master key (user chose "remember device")
    fun setRememberedMaster(password: CharArray) {
        val master = ByteArray(32).also { rng.nextBytes(it) } // 256-bit masterKey
        val wrapped = wrapMaster(master, password)
        SettingsRepository.put(SETTINGS_KEY, wrapped)
    }

    // try unlock: returns masterKey bytes if success, null otherwise
    fun tryUnlock(password: CharArray): ByteArray? {
        val wrapped = SettingsRepository.get(SETTINGS_KEY) ?: return null
        return unwrapMaster(wrapped, password)
    }

    fun clearRemembered() {
        SettingsRepository.delete(SETTINGS_KEY)
    }
}
