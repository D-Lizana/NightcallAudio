package com.nightcallaudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightcallDarkColors = darkColorScheme(
    primary = Color(0xFFB7C4FF),
    onPrimary = Color(0xFF17234F),
    secondary = Color(0xFFC4C6D0),
    background = Color(0xFF090A0F),
    onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF25262E),
)

@Composable
fun NightcallAudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightcallDarkColors,
        typography = Typography,
        content = content,
    )
}
