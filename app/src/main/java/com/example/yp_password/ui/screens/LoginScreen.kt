package com.example.yp_password.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yp_password.ui.PasswordViewModel
import com.example.yp_password.ui.components.PasswordRequirementsBox
import com.example.yp_password.ui.components.isPasswordValid
import com.example.yp_password.ui.theme.AuraBackground
import com.example.yp_password.ui.theme.AuraGradient
import com.example.yp_password.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    viewModel: PasswordViewModel,
    isFirstRun: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Логотип - ОТПЕЧАТОК
        Box(
            modifier = Modifier.size(90.dp).clip(CircleShape).background(AuraGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Fingerprint, null, tint = Color.White, modifier = Modifier.size(56.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Crossfade(targetState = isRegisterMode, label = "Title") { isRegister ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isRegister) "НОВЫЙ АККАУНТ" else "AURA KEY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
                Text(
                    text = if (isRegister) "Регистрация через Email" else "Доступ к данным",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        AuraLoginTextField(value = email, onValueChange = { email = it }, label = "Email", icon = Icons.Rounded.Email)
        Spacer(modifier = Modifier.height(16.dp))
        AuraLoginTextField(value = password, onValueChange = { password = it }, label = "Пароль", icon = Icons.Rounded.Lock, isPassword = true)

        AnimatedVisibility(visible = isRegisterMode) {
            PasswordRequirementsBox(password = password, modifier = Modifier.padding(top = 16.dp))
        }

        AnimatedVisibility(visible = authError != null) {
            Text(authError ?: "", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isFormValid = if (isRegisterMode) email.contains("@") && isPasswordValid(password) else email.isNotEmpty() && password.isNotEmpty()

        Button(
            onClick = {
                if (isRegisterMode) viewModel.register(email, password)
                else viewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(if (isFormValid && !isLoading) AuraGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Gray, Color.Gray))),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text(if (isRegisterMode) "СОЗДАТЬ" else "ВОЙТИ", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isRegisterMode) "Уже есть аккаунт? Войти" else "Нет аккаунта? Регистрация",
            color = Color(0xFF00E5FF),
            modifier = Modifier.clickable { isRegisterMode = !isRegisterMode }
        )
    }
}

@Composable
fun AuraLoginTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPassword: Boolean = false) {
    var isVisible by remember { mutableStateOf(!isPassword) }
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = TextSecondary) },
        singleLine = true, visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = if (label == "Email") KeyboardType.Email else KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color.DarkGray, focusedContainerColor = Color(0xFF1A1A1A), unfocusedContainerColor = Color(0xFF1A1A1A), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
        leadingIcon = { Icon(icon, null, tint = if (value.isNotEmpty()) Color(0xFF00E5FF) else TextSecondary) },
        trailingIcon = { if (isPassword) IconButton(onClick = { isVisible = !isVisible }) { Icon(if (isVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextSecondary) } }
    )
}
