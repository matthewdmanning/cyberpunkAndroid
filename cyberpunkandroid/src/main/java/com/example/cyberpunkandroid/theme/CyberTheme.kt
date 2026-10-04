package com.example.cyberpunkandroid.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberSemanticTokens

// TODO: document this
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
val LocalCyberStatusColors = staticCompositionLocalOf { cyberStatusColors() }

// TODO: document this
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

    val status: CyberStatusColors
        @Composable
        get() = LocalCyberStatusColors.current
}

// TODO: document this
@Composable
fun CyberTheme(
    colors: CyberColors = CyberTheme.colors,
    typography: CyberTypography = CyberTheme.typography,
    shapes: CyberShapes = CyberTheme.shapes,
    semantics: CyberSemanticTokens = CyberTheme.semantics,
    materialColorScheme: ColorScheme = cyberColorScheme(colors, semantics.colors),
    materialTypography: Typography = cyberMaterialTypography(MaterialTheme.typography),
    materialShapes: Shapes = cyberMaterialShapes(),
    status: CyberStatusColors = cyberStatusColors(colors, semantics.colors),
    content: @Composable () -> Unit
) {
    val rememberedColorScheme = remember(materialColorScheme) { materialColorScheme }
    val rememberedTypography = remember(materialTypography) { materialTypography }
    val rememberedShapes = remember(materialShapes) { materialShapes }
    val rememberedStatus = remember(status) { status }

    MaterialTheme(
        colorScheme = rememberedColorScheme,
        typography = rememberedTypography,
        shapes = rememberedShapes
    ) {
        CompositionLocalProvider(
            LocalCyberColors provides colors,
            LocalCyberTypography provides typography,
            LocalCyberShapes provides shapes,
            LocalCyberSemanticTokens provides semantics,
            LocalCyberStatusColors provides rememberedStatus,
            content = content
        )
    }
}
