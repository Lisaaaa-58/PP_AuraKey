plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // подключаем плагин Google Services (но не применяем к корню)
    alias(libs.plugins.google.services) apply false
}
