package com.example.cyberpunkandroid.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberSemanticColors

/**
 * Maps the Cyber palette onto Material 3 color roles so stock Material components inside [CyberTheme]
 * inherit the Cyber palette rather than Material's defaults.
 */
fun cyberColorScheme(
    colors: CyberColors = CyberColors(),
    semantic: CyberSemanticColors = CyberSemanticColors()
): ColorScheme {
    val background = colors.background
    val light = ToneLight
    val onAccent = OnAccent
    val tertiary = CyberPrimitives.Colors.Yellow500

    fun container(accent: Color) = toneContainer(background, accent)
    fun onContainer(accent: Color) = toneOnContainer(accent)

    return darkColorScheme(
        primary = colors.primary,
        onPrimary = onAccent,
        primaryContainer = container(colors.primary),
        onPrimaryContainer = onContainer(colors.primary),
        inversePrimary = lerp(background, colors.primary, 0.45f),
        secondary = colors.secondary,
        onSecondary = onAccent,
        secondaryContainer = container(colors.secondary),
        onSecondaryContainer = onContainer(colors.secondary),
        tertiary = tertiary,
        onTertiary = onAccent,
        tertiaryContainer = container(tertiary),
        onTertiaryContainer = onContainer(tertiary),
        background = background,
        onBackground = colors.textPrimary,
        surface = colors.surfacePrimary,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.surface,
        onSurfaceVariant = colors.textSecondary,
        surfaceTint = colors.primary,
        inverseSurface = light,
        inverseOnSurface = CyberPrimitives.Colors.Void300,
        error = semantic.error,
        onError = onAccent,
        errorContainer = container(semantic.error),
        onErrorContainer = onContainer(semantic.error),
        outline = colors.border,
        outlineVariant = lerp(background, colors.border, 0.5f),
        scrim = CyberPrimitives.Colors.Void900,
        surfaceDim = colors.surfacePrimary,
        surfaceContainerLowest = CyberPrimitives.Colors.Void700,
        surfaceContainerLow = colors.surfaceSecondary,
        surfaceContainer = colors.surfaceTertiary,
        surfaceContainerHigh = colors.surfaceElevated,
        surfaceContainerHighest = colors.surface,
        surfaceBright = lerp(colors.surface, colors.textSecondary, 0.1f),
        primaryFixed = onContainer(colors.primary),
        primaryFixedDim = colors.primary,
        onPrimaryFixed = onAccent,
        onPrimaryFixedVariant = container(colors.primary),
        secondaryFixed = onContainer(colors.secondary),
        secondaryFixedDim = colors.secondary,
        onSecondaryFixed = onAccent,
        onSecondaryFixedVariant = container(colors.secondary),
        tertiaryFixed = onContainer(tertiary),
        tertiaryFixedDim = tertiary,
        onTertiaryFixed = onAccent,
        onTertiaryFixedVariant = container(tertiary)
    )
}

internal val ToneLight = CyberPrimitives.Colors.Chrome100
internal val OnAccent = CyberPrimitives.Colors.Void500
internal fun toneContainer(background: Color, accent: Color): Color = lerp(background, accent, 0.3f)
internal fun toneOnContainer(accent: Color): Color = lerp(accent, ToneLight, 0.5f)
