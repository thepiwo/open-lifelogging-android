package de.thepiwo.lifelogging.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Original app colors from colors.xml
private val YellowGreen = Color(0xFFAFA72E)
private val DarkYellow = Color(0xFFAF972E)
private val Purple = Color(0xFF4E2676)

private val LightColorScheme = lightColorScheme(
    primary = YellowGreen,
    secondary = Purple,
    tertiary = DarkYellow
)

@Composable
fun LifeloggingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}