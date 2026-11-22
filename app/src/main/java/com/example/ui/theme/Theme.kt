package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalBlueLight,
    onPrimary = Color.White,
    primaryContainer = MedicalBlueDark,
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = ClinicalTealLight,
    onSecondary = Color.Black,
    secondaryContainer = ClinicalTealDark,
    onSecondaryContainer = Color(0xFFA7F0E6),
    tertiary = GoldAccentLight,
    onTertiary = Color.Black,
    background = MedicalBackgroundDark,
    onBackground = MedicalTextPrimaryDark,
    surface = MedicalSurfaceDark,
    onSurface = MedicalTextPrimaryDark,
    surfaceVariant = MedicalSurfaceVariantDark,
    onSurfaceVariant = MedicalTextSecondaryDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = ClinicalTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBCEEE6),
    onSecondaryContainer = Color(0xFF00201B),
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = MedicalBackgroundLight,
    onBackground = MedicalTextPrimaryLight,
    surface = MedicalSurfaceLight,
    onSurface = MedicalTextPrimaryLight,
    surfaceVariant = MedicalSurfaceVariantLight,
    onSurfaceVariant = MedicalTextSecondaryLight,
    error = ClinicalError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep medical brand colors consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
