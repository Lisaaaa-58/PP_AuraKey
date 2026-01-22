package com.example.yp_password.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- AURA KEY PALETTE ---

// Фон - не просто черный, а "Void Black"
val AuraBackground = Color(0xFF0A0A0A)
val AuraSurface = Color(0xFF161616)

// Текст
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF888888)

// Градиент "Aura" (Фиолетовый -> Электрик)
val PrimaryViolet = Color(0xFF7000FF)
val PrimaryCyan = Color(0xFF00E5FF)

val AuraGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryViolet, PrimaryCyan)
)

val ErrorRed = Color(0xFFFF453A)
