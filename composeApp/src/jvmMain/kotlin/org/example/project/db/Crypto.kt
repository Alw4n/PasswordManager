package org.example.project.db

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private const val AES_ALGO = "AES/GCM/NoPadding"
    private const val KEY_LEN_BITS = 256
    private const val IV_LEN_BYTES = 12
    private const val TAG_LEN_BITS = 128
    private const val PBKDF2_ITER = 200_000
    private const val SALT_LEN = 16

    private val rng = SecureRandom()

    // derive key from password and salt
    fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITER, KEY_LEN_BITS)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = skf.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    // produce random salt
    fun newSalt(): ByteArray {
        val b = ByteArray(SALT_LEN)
        rng.nextBytes(b)
        return b
    }

    // encrypt plain text; returns base64(salt + iv + ciphertext)
    fun encrypt(plain: String, key: SecretKey, salt: ByteArray? = null): String {
        val iv = ByteArray(IV_LEN_BYTES).also { rng.nextBytes(it) }
        val cipher = Cipher.getInstance(AES_ALGO)
        val gcmSpec = GCMParameterSpec(TAG_LEN_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        val cipherBytes = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))

        // if salt supplied, include it; else prefix zero-length salt (we'll usually pass salt)
        val saltBytes = salt ?: ByteArray(0)
        val out = ByteArray(saltBytes.size + iv.size + cipherBytes.size)
        System.arraycopy(saltBytes, 0, out, 0, saltBytes.size)
        System.arraycopy(iv, 0, out, saltBytes.size, iv.size)
        System.arraycopy(cipherBytes, 0, out, saltBytes.size + iv.size, cipherBytes.size)

        return Base64.getEncoder().encodeToString(out)
    }

    // decrypt base64(salt + iv + ciphertext) — supply salt length used on encrypt
    fun decrypt(b64: String, password: CharArray, saltLen: Int = SALT_LEN): String {
        val all = Base64.getDecoder().decode(b64)
        val salt = all.copyOfRange(0, saltLen)
        val iv = all.copyOfRange(saltLen, saltLen + IV_LEN_BYTES)
        val cipherBytes = all.copyOfRange(saltLen + IV_LEN_BYTES, all.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(AES_ALGO)
        val gcmSpec = GCMParameterSpec(TAG_LEN_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        val plain = cipher.doFinal(cipherBytes)
        return String(plain, Charsets.UTF_8)
    }
}
