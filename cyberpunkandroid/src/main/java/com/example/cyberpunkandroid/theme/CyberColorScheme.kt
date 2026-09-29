package com.example.cyberpunkandroid.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberSemanticColors

/**
 * Maps the Cyber palette onto all 48 Material 3 color roles (material3 1.4.0),
 * so stock Material 3 components (Text, Slider, Surface, …) inside [CyberTheme] render as neon accents
 * on void-black surfaces instead of Material's default purple.
 *
 * Roles with a direct Cyber equivalent reuse it. The rest are interpolated with [lerp], which blends in
 * the perceptual Oklab space, using fractions that approximate Material 3's dark-theme tone targets:
 * accent = tone 80, container = tone 30, on-container = tone 90, background = tone ~6, white = tone 100.
 *
 * Fixed roles keep the same tone in light and dark themes: fixed = tone 90, fixedDim = tone 80,
 * onFixed = tone 10, onFixedVariant = tone 30.
 *
 * @param colors Cyber base colors; overriding these re-derives every dependent role.
 * @param semantic Cyber semantic colors; supplies the error family.
 */
fun cyberColorScheme(
    colors: CyberColors = CyberColors(),
    semantic: CyberSemanticColors = CyberSemanticColors()
): ColorScheme {
    val background = colors.background
    // Near-white anchor used for tone 90 / tone 100 blends and the inverse surface
    val light = CyberPrimitives.Colors.Chrome100
    // Dark text/icons on neon accents (Material's tone 20 "on" colors) for maximum contrast
    val onAccent = CyberPrimitives.Colors.Void500
    // Third brand accent after cyan and magenta; green stays reserved for semantic success
    val tertiary = CyberPrimitives.Colors.Yellow500

    // Container ≈ tone 30: (30 - 6) / (80 - 6) ≈ 0.3 of the way from background to the accent
    fun container(accent: Color) = lerp(background, accent, 0.3f)
    // On-container ≈ tone 90: (90 - 80) / (100 - 80) = 0.5 of the way from the accent to white
    fun onContainer(accent: Color) = lerp(accent, light, 0.5f)

    return darkColorScheme(
        primary = colors.primary,
        onPrimary = onAccent,
        primaryContainer = container(colors.primary),
        onPrimaryContainer = onContainer(colors.primary),
        // Inverse primary ≈ tone 40: (40 - 6) / (80 - 6) ≈ 0.45 from background to primary
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
        // Material 3 now aligns surfaceVariant with surfaceContainerHighest
        surfaceVariant = colors.surface,
        onSurfaceVariant = colors.textSecondary,
        // Elevation tint follows the primary accent, per Material 3 default
        surfaceTint = colors.primary,
        inverseSurface = light,
        // Inverse on-surface ≈ tone 20: a mid-dark void shade
        inverseOnSurface = CyberPrimitives.Colors.Void300,

        error = semantic.error,
        onError = onAccent,
        errorContainer = container(semantic.error),
        onErrorContainer = onContainer(semantic.error),

        outline = colors.border,
        // Outline variant = decorative dividers: halfway between the border and background
        outlineVariant = lerp(background, colors.border, 0.5f),
        // Scrim behind modals: darkest void primitive (Material default is pure black)
        scrim = CyberPrimitives.Colors.Void900,

        // Surface container ramp, darkest to lightest, reusing the existing Void steps
        surfaceDim = colors.surfacePrimary,
        surfaceContainerLowest = CyberPrimitives.Colors.Void700,
        surfaceContainerLow = colors.surfaceSecondary,
        surfaceContainer = colors.surfaceTertiary,
        surfaceContainerHigh = colors.surfaceElevated,
        surfaceContainerHighest = colors.surface,
        // Surface bright ≈ tone 24, one small step above the highest container (tone 22)
        surfaceBright = lerp(colors.surface, colors.textSecondary, 0.1f),

        // Fixed roles: fixed = tone 90 (same blend as on-container), fixedDim = the tone 80 accent,
        // onFixed = tone 10 (near-black void), onFixedVariant = tone 30 (same blend as container)
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
