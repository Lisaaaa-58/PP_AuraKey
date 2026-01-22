package com.example.yp_password.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.yp_password.domain.Password
import com.example.yp_password.security.CloudCrypto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PasswordViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _passwordList = MutableStateFlow<List<Password>>(emptyList())
    val passwordList = _passwordList.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var sessionKey = ""

    // --- ВХОД ---
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                sessionKey = pass
                _isAuthenticated.value = true
                _authError.value = null
                listenToCloudData()
            } catch (e: Exception) {
                _authError.value = "Ошибка: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    // --- РЕГИСТРАЦИЯ ---
    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.createUserWithEmailAndPassword(email, pass).await()
                sessionKey = pass
                _isAuthenticated.value = true
                _authError.value = null
                listenToCloudData() // Важно: начинаем слушать базу сразу
            } catch (e: Exception) {
                _authError.value = "Ошибка: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    // --- ВЫХОД ---
    fun logout() {
        auth.signOut()
        sessionKey = ""
        _isAuthenticated.value = false
        _passwordList.value = emptyList()
    }

    // --- СЛУШАТЕЛЬ БАЗЫ ---
    private fun listenToCloudData() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("passwords")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        Password(
                            id = doc.id.hashCode(),
                            firestoreId = doc.id,
                            title = doc.getString("title") ?: "",
                            login = doc.getString("login") ?: "",
                            password = CloudCrypto.decrypt(doc.getString("enc_pass") ?: "", sessionKey),
                            notes = CloudCrypto.decrypt(doc.getString("enc_notes") ?: "", sessionKey),
                            category = "General",
                            colorHex = "#00E5FF",
                            createDate = doc.getLong("date") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                _passwordList.value = list
            }
    }

    fun addPassword(title: String, login: String, pass: String, notes: String) {
        val userId = auth.currentUser?.uid ?: return
        val data = hashMapOf(
            "title" to title,
            "login" to login,
            "enc_pass" to CloudCrypto.encrypt(pass, sessionKey),
            "enc_notes" to CloudCrypto.encrypt(notes, sessionKey),
            "date" to System.currentTimeMillis()
        )
        db.collection("users").document(userId).collection("passwords").add(data)
    }

    fun updatePassword(firestoreId: String, title: String, login: String, pass: String, notes: String) {
        val userId = auth.currentUser?.uid ?: return
        if (firestoreId.isEmpty()) return

        val data = mapOf(
            "title" to title,
            "login" to login,
            "enc_pass" to CloudCrypto.encrypt(pass, sessionKey),
            "enc_notes" to CloudCrypto.encrypt(notes, sessionKey)
        )
        db.collection("users").document(userId).collection("passwords").document(firestoreId).update(data)
    }

    fun deletePassword(firestoreId: String) {
        val userId = auth.currentUser?.uid ?: return
        if (firestoreId.isNotEmpty()) {
            db.collection("users").document(userId).collection("passwords").document(firestoreId).delete()
        }
    }

    // --- НАСТОЯЩАЯ СМЕНА МАСТЕР-ПАРОЛЯ ---
    fun changeMasterPassword(oldPass: String, newPass: String, callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        val userId = user?.uid

        Log.d("AuraKey", "Начинаем смену пароля...")

        if (userId == null) {
            Log.e("AuraKey", "Ошибка: Пользователь не найден")
            callback(false)
            return
        }

        if (oldPass != sessionKey) {
            Log.e("AuraKey", "Ошибка: Старый пароль не совпадает с ключом сессии")
            callback(false)
            return
        }

        viewModelScope.launch {
            try {
                // 1. Повторная авторизация (Требование Firebase)
                Log.d("AuraKey", "1. Проверка старого пароля в облаке...")
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPass)
                user.reauthenticate(credential).await()
                Log.d("AuraKey", "-> Пароль верный.")

                // 2. Перешифровка базы
                Log.d("AuraKey", "2. Начало перешифровки данных...")
                val currentData = _passwordList.value
                val batch = db.batch()

                for (item in currentData) {
                    val docRef = db.collection("users").document(userId).collection("passwords").document(item.firestoreId)

                    val newData = mapOf(
                        "enc_pass" to CloudCrypto.encrypt(item.password, newPass),
                        "enc_notes" to CloudCrypto.encrypt(item.notes, newPass)
                    )
                    batch.update(docRef, newData)
                }

                // Отправка в базу
                Log.d("AuraKey", "3. Отправка новых данных в базу...")
                batch.commit().await()
                Log.d("AuraKey", "-> База обновлена.")

                // 3. Смена пароля аккаунта
                Log.d("AuraKey", "4. Смена пароля аккаунта (Auth)...")
                user.updatePassword(newPass).await()
                Log.d("AuraKey", "-> Пароль аккаунта изменен.")

                // 4. Финиш
                sessionKey = newPass
                Log.d("AuraKey", "УСПЕХ! Всё готово.")
                callback(true)

            } catch (e: Exception) {
                Log.e("AuraKey", "КРИТИЧЕСКАЯ ОШИБКА: ${e.message}")
                e.printStackTrace()
                callback(false)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { PasswordViewModel() }
        }
    }
}
