package com.example.cyberpunkandroid.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
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
val LocalCyberElevation = staticCompositionLocalOf { CyberElevation() }
val LocalCyberSemanticTokens = staticCompositionLocalOf { CyberSemanticTokens() }
val LocalCyberStatusColors = staticCompositionLocalOf { cyberStatusColors() }

object CyberTheme {
    val colors: CyberColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberColors.current

    val typography: CyberTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberTypography.current

    val shapes: CyberShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberShapes.current

    val elevation: CyberElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberElevation.current

    val spacing: CyberSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberSpacing.current

    val semantics: CyberSemanticTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberSemanticTokens.current

    val status: CyberStatusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCyberStatusColors.current
}

val MaterialTheme.cyberColors: CyberColors
    @Composable
    @ReadOnlyComposable
    get() = CyberTheme.colors

val MaterialTheme.cyberTypography: CyberTypography
    @Composable
    @ReadOnlyComposable
    get() = CyberTheme.typography

val MaterialTheme.cyberShapes: CyberShapes
    @Composable
    @ReadOnlyComposable
    get() = CyberTheme.shapes

val MaterialTheme.cyberElevation: CyberElevation
    @Composable
    @ReadOnlyComposable
    get() = CyberTheme.elevation

val MaterialTheme.cyberSpacing: CyberSpacing
    @Composable
    @ReadOnlyComposable
    get() = CyberTheme.spacing

@Composable
fun CyberTheme(
    colors: CyberColors = CyberTheme.colors,
    typography: CyberTypography = CyberTheme.typography,
    shapes: CyberShapes = CyberTheme.shapes,
    elevation: CyberElevation = CyberTheme.elevation,
    spacing: CyberSpacing = CyberTheme.spacing,
    semantics: CyberSemanticTokens = CyberTheme.semantics,
    materialColorScheme: ColorScheme = cyberColorScheme(colors, semantics.colors),
    materialTypography: Typography = cyberMaterialTypography(MaterialTheme.typography),
    materialShapes: Shapes = cyberMaterialShapes(),
    status: CyberStatusColors = cyberStatusColors(colors, semantics.colors),
    wrapMaterialTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val rememberedElevation = remember(elevation) { elevation }
    val rememberedSpacing = remember(spacing) { spacing }
    val rememberedColorScheme = remember(materialColorScheme) { materialColorScheme }
    val rememberedTypography = remember(materialTypography) { materialTypography }
    val rememberedShapes = remember(materialShapes) { materialShapes }
    val rememberedStatus = remember(status) { status }

    val contentWithProviders = @Composable {
        CompositionLocalProvider(
            LocalCyberColors provides colors,
            LocalCyberTypography provides typography,
            LocalCyberShapes provides shapes,
            LocalCyberElevation provides rememberedElevation,
            LocalCyberSpacing provides rememberedSpacing,
            LocalCyberSemanticTokens provides semantics,
            LocalCyberStatusColors provides rememberedStatus,
            LocalContentColor provides colors.textPrimary
        ) {
            ProvideTextStyle(value = typography.body) {
                content()
            }
        }
    }

    if (wrapMaterialTheme) {
        MaterialTheme(
            colorScheme = rememberedColorScheme,
            typography = rememberedTypography,
            shapes = rememberedShapes
        ) {
            contentWithProviders()
        }
    } else {
        contentWithProviders()
    }
}
