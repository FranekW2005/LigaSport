package com.example.ligasport.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ==========================================
// TWOJE KOLORY - TEMAT PIŁKARSKI
// ==========================================

// Główne tło - ciemna zieleń (murawa wieczorem)
val BackgroundGreen = Color(0xFF1A2E1A)

// Akcent 1 - pomarańcz (pachołki/znaczniki)
val Orange = Color(0xFFFF8C00)

// Akcent 2 - niebieski (Firebase/technologia)
val Blue = Color(0xFF4A90E2)

// Tekst - off-white (kreda na boisku)
val OffWhite = Color(0xFFF5F5F5)

// Karty/moduły - jaśniejsza zieleń
val CardGreen = Color(0xFF243B24)

// Dodatkowe kolory dla kompletności
val DarkGreen = Color(0xFF0D1F0D)   // Bardzo ciemna zieleń (przyciski)
val LightOrange = Color(0xFFFFA726) // Jaśniejszy pomarańcz (hover)

// ==========================================
// CIEMNY SCHEMAT (NASZ GŁÓWNY)
// ==========================================
private val DarkColorScheme = darkColorScheme(
    // Kolory podstawowe
    primary = Orange,
    onPrimary = OffWhite,
    primaryContainer = Orange,
    onPrimaryContainer = OffWhite,

    // Kolory wtórne
    secondary = Blue,
    onSecondary = OffWhite,
    secondaryContainer = Blue,
    onSecondaryContainer = OffWhite,

    // Kolory trzeciorzędne
    tertiary = LightOrange,
    onTertiary = OffWhite,

    // Tło
    background = BackgroundGreen,
    onBackground = OffWhite,

    // Powierzchnie (karty, dialogi, bottom bar)
    surface = CardGreen,
    onSurface = OffWhite,
    surfaceVariant = CardGreen,
    onSurfaceVariant = OffWhite.copy(alpha = 0.7f),

    // Kontenery
    surfaceContainerLow = DarkGreen,
    surfaceContainer = CardGreen,
    surfaceContainerHigh = CardGreen,

    // Kolory błędów
    error = Color(0xFFFF5252),
    onError = OffWhite,

    // Kolary outline (obramowania)
    outline = OffWhite.copy(alpha = 0.3f),
    outlineVariant = OffWhite.copy(alpha = 0.15f)
)

// ==========================================
// JASNY SCHEMAT (na wszelki wypadek)
// ==========================================
private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Blue,
    onSecondary = Color.White,
    tertiary = LightOrange,
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = BackgroundGreen,
    surface = Color.White,
    onSurface = BackgroundGreen,
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = BackgroundGreen.copy(alpha = 0.7f),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    outline = BackgroundGreen.copy(alpha = 0.3f)
)

// ==========================================
// MOTYW APLIKACJI
// ==========================================
@Composable
fun LigaSportTheme(
    darkTheme: Boolean = true,  // ZAWSZE ciemny (piłkarski klimat)
    dynamicColor: Boolean = false,  // Wyłączone - używamy NASZYCH kolorów
    content: @Composable () -> Unit
) {
    // Zawsze używaj naszego ciemnego schematu
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}