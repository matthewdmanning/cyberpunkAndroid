package com.example.cyberpunkandroid.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.config.CyberSemanticColors

/**
 * One status color expanded into Material 3's four-role pattern (the same shape as error / onError /
 * errorContainer / onErrorContainer).
 *
 * @property color Full-strength neon status color (tone 80), e.g. an icon or badge fill.
 * @property onColor Dark void text/icons drawn on top of [color].
 * @property container Dim tinted fill (≈ tone 30), e.g. a status banner background.
 * @property onContainer Pale tinted text/icons (≈ tone 90) drawn on top of [container].
 */
@Immutable
data class CyberStatusRole(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color
)

/**
 * Status colors Material 3 lacks (it only defines error), each expanded into a [CyberStatusRole] with the
 * same interpolation as the Material error roles in [cyberColorScheme].
 */
@Immutable
data class CyberStatusColors(
    val success: CyberStatusRole,
    val info: CyberStatusRole,
    val warning: CyberStatusRole,
    val caution: CyberStatusRole,
    val danger: CyberStatusRole
)

/**
 * Builds [CyberStatusColors] from the Cyber semantic colors, blending containers against [CyberColors.background].
 */
fun cyberStatusColors(
    colors: CyberColors = CyberColors(),
    semantic: CyberSemanticColors = CyberSemanticColors()
): CyberStatusColors {
    fun role(accent: Color) = CyberStatusRole(
        color = accent,
        onColor = OnAccent,
        container = toneContainer(colors.background, accent),
        onContainer = toneOnContainer(accent)
    )

    return CyberStatusColors(
        success = role(semantic.success),
        info = role(semantic.info),
        warning = role(semantic.warning),
        caution = role(semantic.caution),
        danger = role(semantic.danger)
    )
}
