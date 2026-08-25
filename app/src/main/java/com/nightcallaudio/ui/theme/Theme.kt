package com.nightcallaudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightcallDarkColors = darkColorScheme(
    primary = NightcallPink,
    onPrimary = NightcallOnPink,
    primaryContainer = NightcallPinkContainer,
    onPrimaryContainer = NightcallOnPinkContainer,
    secondary = NightcallCyan,
    onSecondary = NightcallOnCyan,
    secondaryContainer = NightcallCyanContainer,
    onSecondaryContainer = NightcallOnCyanContainer,
    tertiary = NightcallCoral,
    onTertiary = NightcallOnCoral,
    tertiaryContainer = NightcallCoralContainer,
    onTertiaryContainer = NightcallOnCoralContainer,
    background = NightcallBackground,
    onBackground = NightcallOnBackground,
    surface = NightcallSurface,
    onSurface = NightcallOnSurface,
    surfaceVariant = NightcallSurfaceContainerHigh,
    onSurfaceVariant = NightcallOnSurfaceVariant,
    surfaceDim = NightcallSurfaceDim,
    surfaceBright = NightcallSurfaceBright,
    surfaceContainerLowest = NightcallSurfaceContainerLowest,
    surfaceContainerLow = NightcallSurfaceContainerLow,
    surfaceContainer = NightcallSurfaceContainer,
    surfaceContainerHigh = NightcallSurfaceContainerHigh,
    surfaceContainerHighest = NightcallSurfaceContainerHighest,
    outline = NightcallOutline,
    outlineVariant = NightcallOutlineVariant,
    inverseSurface = NightcallOnSurface,
    inverseOnSurface = NightcallSurface,
    inversePrimary = NightcallPinkContainer,
    scrim = Color.Black,
)

@Composable
fun NightcallAudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NightcallDarkColors,
        typography = Typography,
        content = content,
    )
}
