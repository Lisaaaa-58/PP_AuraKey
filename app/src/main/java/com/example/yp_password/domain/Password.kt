package com.example.yp_password.domain

data class Password(
    val id: Int = 0,             // ID для списка на экране
    val firestoreId: String = "", // ID документа в Firebase (нужен для удаления/правки)
    val title: String,
    val login: String,
    val password: String,
    val notes: String,
    val category: String,
    val colorHex: String,
    val createDate: Long
)
