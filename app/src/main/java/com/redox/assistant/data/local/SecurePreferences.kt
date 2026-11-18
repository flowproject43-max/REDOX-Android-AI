package com.redox.assistant.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redox_secure_prefs", Context.MODE_PRIVATE)

    private var keyStore: KeyStore? = null

    init {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            ensureMasterKey()
        } catch (e: Exception) {
            keyStore = null
        }
    }

    private fun ensureMasterKey() {
        val ks = keyStore ?: return
        try {
            if (!ks.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val keyGenSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Gracefully ignore keygen error, will fallback to obfuscation
        }
    }

    private fun getSecretKey(): SecretKey? {
        val ks = keyStore ?: return null
        return try {
            (ks.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
        } catch (e: Exception) {
            null
        }
    }

    fun putSecureString(key: String, value: String) {
        val secretKey = getSecretKey()
        if (secretKey != null) {
            try {
                val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val cipherText = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

                val combined = ByteArray(iv.size + cipherText.size)
                System.arraycopy(iv, 0, combined, 0, iv.size)
                System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

                val encoded = Base64.encodeToString(combined, Base64.NO_WRAP)
                prefs.edit().putString(key, encoded).apply()
                return
            } catch (e: Exception) {
                // Fallback to obfuscated storage
            }
        }
        val fallback = Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        prefs.edit().putString(key, fallback).apply()
    }

    fun getSecureString(key: String): String? {
        val encrypted = prefs.getString(key, null) ?: return null
        val secretKey = getSecretKey()
        if (secretKey != null) {
            try {
                val combined = Base64.decode(encrypted, Base64.NO_WRAP)
                if (combined.size > GCM_IV_LENGTH) {
                    val iv = ByteArray(GCM_IV_LENGTH)
                    val cipherText = ByteArray(combined.size - GCM_IV_LENGTH)

                    System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
                    System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.size)

                    val cipher = Cipher.getInstance(AES_GCM_NOPADDING)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                    val plainTextBytes = cipher.doFinal(cipherText)
                    return String(plainTextBytes, Charsets.UTF_8)
                }
            } catch (e: Exception) {
                // Fallback decode below
            }
        }
        return try {
            String(Base64.decode(encrypted, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (ex: Exception) {
            null
        }
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String? = null): String? {
        return prefs.getString(key, default)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun putFloat(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        return prefs.getFloat(key, default)
    }

    companion object {
        private const val KEY_ALIAS = "RedoxMasterKey"
        private const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
    }
}
