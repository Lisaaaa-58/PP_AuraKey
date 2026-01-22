package com.example.yp_password.security

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CloudCrypto {

    // превращаем пароль в ключ 256 бит
    private fun generateKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = password.toByteArray(Charsets.UTF_8)
        digest.update(bytes, 0, bytes.size)
        val key = digest.digest()
        return SecretKeySpec(key, "AES")
    }

    // шифруем
    fun encrypt(data: String, masterPassword: String): String {
        if (data.isEmpty()) return ""
        try {
            val key = generateKey(masterPassword)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16)
            java.security.SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // расшифровываем
    fun decrypt(encryptedData: String, masterPassword: String): String {
        if (encryptedData.isEmpty()) return ""
        try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)

            val iv = ByteArray(16)
            System.arraycopy(combined, 0, iv, 0, 16)
            val ivSpec = IvParameterSpec(iv)

            val encrypted = ByteArray(combined.size - 16)
            System.arraycopy(combined, 16, encrypted, 0, encrypted.size)

            val key = generateKey(masterPassword)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            return String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            return "Error (Wrong Password?)"
        }
    }
}
