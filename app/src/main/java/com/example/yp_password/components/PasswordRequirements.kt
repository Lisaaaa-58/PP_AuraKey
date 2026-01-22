package com.example.yp_password.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yp_password.ui.theme.TextSecondary

// Модель правила
data class ValidationRule(
    val label: String,
    val check: (String) -> Boolean
)

// Список правил
val PASSWORD_RULES = listOf(
    ValidationRule("Минимум 8 символов") { it.length >= 8 },
    ValidationRule("Заглавная буква (A-Z)") { it.any { char -> char.isUpperCase() } },
    ValidationRule("Цифра (0-9)") { it.any { char -> char.isDigit() } },
    ValidationRule("Спецсимвол (!@#$%)") { it.any { char -> !char.isLetterOrDigit() && !char.isWhitespace() } },
    ValidationRule("Без пробелов") { it.isNotEmpty() && it.none { char -> char.isWhitespace() } }
)

@Composable
fun PasswordRequirementsBox(
    password: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Требования к паролю:",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PASSWORD_RULES.forEach { rule ->
            RequirementItem(label = rule.label, isMet = rule.check(password))
        }
    }
}

@Composable
fun RequirementItem(label: String, isMet: Boolean) {
    // Анимация цвета: Серый -> Неоновый
    val color by animateColorAsState(
        targetValue = if (isMet) Color(0xFF00E5FF) else Color.DarkGray,
        label = "color"
    )

    val icon = if (isMet) Icons.Rounded.Check else Icons.Rounded.Close

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = if (isMet) Color.White else TextSecondary,
            fontSize = 12.sp
        )
    }
}

// Функция для проверки всего пароля (для блокировки кнопки)
fun isPasswordValid(password: String): Boolean {
    return PASSWORD_RULES.all { it.check(password) }
}
