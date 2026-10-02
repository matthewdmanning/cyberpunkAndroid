package com.example.cyberpunkandroid.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberSemanticTokens

@Immutable
data class CyberColors(
    val primary: Color = CyberPrimitives.Colors.Cyan500,
    val secondary: Color = CyberPrimitives.Colors.Magenta500,
    val background: Color = CyberPrimitives.Colors.Void500,
    val surface: Color = CyberPrimitives.Colors.Void100,
    val surfacePrimary: Color = CyberPrimitives.Colors.Void500,
    val surfaceSecondary: Color = CyberPrimitives.Colors.Void400,
    val surfaceTertiary: Color = CyberPrimitives.Colors.Void300,
    val surfaceElevated: Color = CyberPrimitives.Colors.Void200,
    val textPrimary: Color = CyberPrimitives.Colors.Cyan500,
    val textSecondary: Color = CyberPrimitives.Colors.Chrome500,
    val border: Color = CyberPrimitives.Colors.Cyan700
)

val LocalCyberColors = staticCompositionLocalOf { CyberColors() }
val LocalCyberTypography = staticCompositionLocalOf { CyberTypography() }
val LocalCyberShapes = staticCompositionLocalOf { CyberShapes() }
val LocalCyberSemanticTokens = staticCompositionLocalOf { CyberSemanticTokens() }

object CyberTheme {
    val colors: CyberColors
        @Composable
        get() = LocalCyberColors.current

    val typography: CyberTypography
        @Composable
        get() = LocalCyberTypography.current

    val shapes: CyberShapes
        @Composable
        get() = LocalCyberShapes.current

    val semantics: CyberSemanticTokens
        @Composable
        get() = LocalCyberSemanticTokens.current
}

@Composable
fun CyberTheme(
    colors: CyberColors = CyberTheme.colors,
    typography: CyberTypography = CyberTheme.typography,
    shapes: CyberShapes = CyberTheme.shapes,
    semantics: CyberSemanticTokens = CyberTheme.semantics,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = cyberMaterialTypography(MaterialTheme.typography),
        shapes = MaterialTheme.shapes
    ) {
        CompositionLocalProvider(
            LocalCyberColors provides colors,
            LocalCyberTypography provides typography,
            LocalCyberShapes provides shapes,
            LocalCyberSemanticTokens provides semantics,
            content = content
        )
    }
}
