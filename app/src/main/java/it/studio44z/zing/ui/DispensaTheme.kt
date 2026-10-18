package it.studio44z.zing.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Leaf = Color(0xFF287A59)
private val LeafLight = Color(0xFFDCEFE0)
private val Cream = Color(0xFFF6F7F2)
private val Ink = Color(0xFF18251D)
private val Muted = Color(0xFF68746C)
private val Peach = Color(0xFFF4DCC9)
private val Amber = Color(0xFFB76D1F)
private val DarkBg = Color(0xFF101912)
private val DarkSurface = Color(0xFF1A261E)
private val DarkLeaf = Color(0xFF94D6AE)

private val LightColors = lightColorScheme(
    primary = Leaf,
    onPrimary = Color.White,
    primaryContainer = LeafLight,
    onPrimaryContainer = Color(0xFF123D2B),
    secondary = Color(0xFF65795F),
    onSecondary = Color.White,
    tertiary = Amber,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8ECE5),
    onSurfaceVariant = Muted,
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = DarkLeaf,
    onPrimary = Color(0xFF143D2B),
    primaryContainer = Color(0xFF254D38),
    onPrimaryContainer = Color(0xFFD8F5E0),
    secondary = Color(0xFFB5C9B4),
    tertiary = Color(0xFFF4C28C),
    background = DarkBg,
    onBackground = Color(0xFFE6EDE6),
    surface = DarkSurface,
    onSurface = Color(0xFFE6EDE6),
    surfaceVariant = Color(0xFF2A372E),
    onSurfaceVariant = Color(0xFFBDC9BE),
    error = Color(0xFFFFB4AB),
)

@Composable
fun DispensaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
