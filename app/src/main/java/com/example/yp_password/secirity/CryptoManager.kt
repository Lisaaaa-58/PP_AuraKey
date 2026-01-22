package com.example.yp_password.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION)
    private val decryptCipher get() = Cipher.getInstance(TRANSFORMATION)

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, "AndroidKeyStore").apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    // Шифруем строку -> возвращаем Base64 строку (IV + Data)
    fun encrypt(data: String): String {
        if (data.isEmpty()) return ""

        val bytes = data.toByteArray(Charsets.UTF_8)
        val cipher = encryptCipher
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(bytes)

        // Соединяем IV и данные, чтобы потом расшифровать
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    // Расшифровываем Base64 строку -> возвращаем обычный текст
    fun decrypt(encryptedData: String): String {
        if (encryptedData.isEmpty()) return ""

        try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            // GCM IV всегда 12 байт
            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)

            val encryptedBytes = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.size)

            val cipher = decryptCipher
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

            val decoded = cipher.doFinal(encryptedBytes)
            return String(decoded, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error"
        }
    }

    companion object {
        private const val ALIAS = "secret_password_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
