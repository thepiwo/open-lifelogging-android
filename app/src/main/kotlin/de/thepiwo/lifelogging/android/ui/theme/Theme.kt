package de.thepiwo.lifelogging.android.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Original app colors from colors.xml
private val YellowGreen = Color(0xFFAFA72E)
private val DarkYellow = Color(0xFFAF972E)
private val Purple = Color(0xFF4E2676)

private val LightColorPalette = lightColors(
    primary = YellowGreen,
    primaryVariant = DarkYellow,
    secondary = Purple,
    secondaryVariant = Purple
)

@Composable
fun LifeloggingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColorPalette,
        content = content
    )
}