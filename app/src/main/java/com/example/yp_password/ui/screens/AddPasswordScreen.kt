package com.example.yp_password.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.yp_password.domain.Password
import com.example.yp_password.ui.PasswordViewModel
import com.example.yp_password.ui.components.PasswordRequirementsBox
import com.example.yp_password.ui.components.isPasswordValid
import com.example.yp_password.ui.theme.AuraBackground
import com.example.yp_password.ui.theme.AuraGradient
import com.example.yp_password.ui.theme.AuraSurface
import com.example.yp_password.ui.theme.TextPrimary
import com.example.yp_password.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordScreen(viewModel: PasswordViewModel, onLogout: () -> Unit) {
    val passwords by viewModel.passwordList.collectAsState()

    var passwordToEdit by remember { mutableStateOf<Password?>(null) }
    var passwordToDelete by remember { mutableStateOf<Password?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        containerColor = AuraBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AURA KEY", fontWeight = FontWeight.Bold, letterSpacing = 2.sp, color = Color.White) },
                actions = { IconButton(onClick = { showSettingsSheet = true }) { Icon(Icons.Rounded.Settings, "Settings", tint = TextSecondary) } },
                navigationIcon = { IconButton(onClick = onLogout) { Icon(Icons.Rounded.Logout, "Logout", tint = TextSecondary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AuraBackground)
            )
        },
        floatingActionButton = {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(AuraGradient).clickable { passwordToEdit = null; showAddSheet = true }, contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        if (passwords.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("Облако пусто", color = TextSecondary) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { Spacer(Modifier.height(8.dp)) }
                items(passwords) { item ->
                    AuraPasswordCard(
                        item = item,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(item.password))
                            if (android.os.Build.VERSION.SDK_INT < 33) Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = { passwordToEdit = item; showAddSheet = true },
                        onDelete = { passwordToDelete = item }
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }

        if (passwordToDelete != null) {
            DeleteConfirmationDialog(
                item = passwordToDelete!!,
                onConfirm = { viewModel.deletePassword(passwordToDelete!!.firestoreId); passwordToDelete = null },
                onDismiss = { passwordToDelete = null }
            )
        }

        if (showAddSheet) {
            AddEditPasswordBottomSheet(
                passwordToEdit = passwordToEdit,
                onDismiss = { showAddSheet = false },
                onSave = { firestoreId, title, login, pass, notes ->
                    if (firestoreId.isEmpty()) viewModel.addPassword(title, login, pass, notes)
                    else viewModel.updatePassword(firestoreId, title, login, pass, notes)
                    showAddSheet = false
                }
            )
        }

        if (showSettingsSheet) {
            ChangeMasterPasswordSheet(viewModel, { showSettingsSheet = false })
        }
    }
}

@Composable
fun DeleteConfirmationDialog(item: Password, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismiss) {
        Card(colors = CardDefaults.cardColors(AuraSurface), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color(0xFF333333))) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Warning, null, tint = Color(0xFFFF453A), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("Удалить?", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                Text("Удалить \"${item.title}\" из облака?", color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Color(0xFF222222)), shape = RoundedCornerShape(12.dp)) { Text("Отмена") }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = onConfirm, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(Color(0xFFFF453A)), shape = RoundedCornerShape(12.dp)) { Text("Удалить") }
                }
            }
        }
    }
}

@Composable
fun AuraPasswordCard(item: Password, onCopy: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showPassword by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(AuraSurface), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFF252525)), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF222222)), contentAlignment = Alignment.Center) { Text(item.title.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (item.login.isNotBlank()) Text(item.login, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Row { IconButton(onEdit) { Icon(Icons.Rounded.Edit, null, tint = TextSecondary) }; IconButton(onDelete) { Icon(Icons.Rounded.Delete, null, tint = Color(0xFF553333)) } }
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F0F0F)).clickable { onCopy() }.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showPassword) item.password else "••••••••••••", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Row { IconButton(onClick = { showPassword = !showPassword }, Modifier.size(24.dp)) { Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextSecondary) }; Spacer(Modifier.width(8.dp)); Icon(Icons.Rounded.ContentCopy, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordBottomSheet(passwordToEdit: Password?, onDismiss: () -> Unit, onSave: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf(passwordToEdit?.title ?: "") }
    var login by remember { mutableStateOf(passwordToEdit?.login ?: "") }
    var password by remember { mutableStateOf(passwordToEdit?.password ?: "") }
    var notes by remember { mutableStateOf(passwordToEdit?.notes ?: "") }
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(rotationAngle)

    ModalBottomSheet(onDismiss, containerColor = AuraSurface) {
        Column(Modifier.padding(24.dp).navigationBarsPadding()) {
            Text(if (passwordToEdit == null) "Новая запись" else "Редактирование", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(24.dp))
            AuraTextField(title, { title = it }, "Название")
            Spacer(Modifier.height(12.dp))
            AuraTextField(login, { login = it }, "Логин")
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Box(Modifier.weight(1f)) { AuraTextField(password, { password = it }, "Пароль", isPassword = true) }
                Spacer(Modifier.width(8.dp))
                IconButton({ password = generateSecurePassword(); rotationAngle += 360f }, Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF222222))) { Icon(Icons.Rounded.Casino, null, tint = Color(0xFF00E5FF), modifier = Modifier.rotate(animatedRotation)) }
            }
            Spacer(Modifier.height(12.dp))
            AuraTextField(notes, { notes = it }, "Заметки")
            Spacer(Modifier.height(32.dp))
            Button({ onSave(passwordToEdit?.firestoreId ?: "", title, login, password, notes) }, Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(AuraGradient), colors = ButtonDefaults.buttonColors(Color.Transparent)) { Text("Сохранить", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun AuraTextField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false) {
    var isVisible by remember { mutableStateOf(!isPassword) }
    Column {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        OutlinedTextField(
            value, onValueChange, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00E5FF), unfocusedBorderColor = Color(0xFF333333), focusedContainerColor = Color(0xFF111111), unfocusedContainerColor = Color(0xFF111111), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = { if (isPassword) IconButton({ isVisible = !isVisible }) { Icon(if (isVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null, tint = TextSecondary) } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeMasterPasswordSheet(viewModel: PasswordViewModel, onDismiss: () -> Unit) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AuraSurface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Text("Безопасность", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))

            if (success) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Пароль успешно перешифрован", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))) { Text("Закрыть") }
                    }
                }
            } else {
                AuraTextField(value = oldPass, onValueChange = { oldPass = it }, label = "Старый пароль", isPassword = true)
                Spacer(modifier = Modifier.height(12.dp))
                AuraTextField(value = newPass, onValueChange = { newPass = it }, label = "Новый пароль", isPassword = true)

                // Валидация
                Spacer(modifier = Modifier.height(16.dp))
                PasswordRequirementsBox(password = newPass)

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        error = ""
                        viewModel.changeMasterPassword(oldPass, newPass) { isSuccess ->
                            isLoading = false
                            if (isSuccess) {
                                success = true
                            } else {
                                error = "Ошибка. Проверьте старый пароль."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(12.dp)).background(if (isPasswordValid(newPass) && !isLoading) AuraGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Gray, Color.Gray))),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isLoading && oldPass.isNotEmpty() && isPasswordValid(newPass)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Перешифровать базу", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun generateSecurePassword(): String = (1..16).map { "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%".random() }.joinToString("")
