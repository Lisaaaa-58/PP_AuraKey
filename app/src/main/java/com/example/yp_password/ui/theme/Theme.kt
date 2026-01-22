package com.example.yp_password.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Используем наши новые цвета Aura
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,    // Фиолетовый акцент
    secondary = PrimaryCyan,    // Голубой акцент
    background = AuraBackground, // Глубокий черный (0A0A0A)
    surface = AuraSurface,       // Чуть светлее (161616)
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun YP_PasswordTheme(
    content: @Composable () -> Unit
) {
    // Всегда используем темную схему
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Красим статус бар в цвет фона
            window.statusBarColor = AuraBackground.toArgb()
            window.navigationBarColor = AuraBackground.toArgb()

            // Делаем иконки статус бара светлыми (так как фон темный)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
