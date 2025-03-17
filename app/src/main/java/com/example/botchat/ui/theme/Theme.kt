package com.example.botchat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Định nghĩa LightColorScheme với màu nền trắng
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111011),
    secondary = Color(0xFF03DAC6),
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Định nghĩa DarkColorScheme với màu nền đen sâu AMOLED
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFDFDFD),
    secondary = Color(0xFFF2F8F8),
    background = Color(0xFF0E0D0D),    // Màu đen sâu AMOLED
    surface =Color(0xFF0E0D0D),       // Màu bề mặt đen sâu, đồng bộ với background
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,        // Màu chữ/icon trắng trên nền đen
    onSurface = Color.White            // Màu chữ/icon trắng trên bề mặt
)

@Composable
fun BotChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) {
                Color(0xFF0E0D0D).toArgb() // Màu đen sâu AMOLED cho Dark Mode
            } else {
                Color.White.toArgb()        // Màu trắng cho Light Mode
            }
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}