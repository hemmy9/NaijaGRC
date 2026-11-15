package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Gold500,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold600,
    tertiary = Sapphire700,
    onTertiary = Color.White,
    tertiaryContainer = Sapphire100,
    onTertiaryContainer = Ink950,
    background = AlabasterBg,
    onBackground = Ink950,
    surface = AlabasterCard,
    onSurface = Ink950,
    surfaceVariant = AlabasterElevated,
    onSurfaceVariant = Ink700,
    outline = SlateBorderLight,
    outlineVariant = Color(0xFFE5EEE9),
    error = CrimsonError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Emerald950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = Gold400,
    onSecondary = Emerald950,
    secondaryContainer = Gold600,
    onSecondaryContainer = Gold100,
    tertiary = Sapphire500,
    onTertiary = Color.White,
    tertiaryContainer = Sapphire700,
    onTertiaryContainer = Sapphire100,
    background = ObsidianBg,
    onBackground = Mist100,
    surface = ObsidianSurface,
    onSurface = Mist100,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = Mist300,
    outline = SlateBorderDark,
    outlineVariant = Color(0xFF153025),
    error = CrimsonErrorDark,
    onError = Emerald950
)

val NaijaGrcShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NaijaGrcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NaijaGrcTypography,
        shapes = NaijaGrcShapes,
        content = content
    )
}
