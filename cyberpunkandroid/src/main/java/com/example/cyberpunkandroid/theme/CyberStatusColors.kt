package com.example.cyberpunkandroid.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.cyberpunkandroid.config.CyberSemanticColors

@Immutable
data class CyberStatusRole(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color
)

@Immutable
data class CyberStatusColors(
    val success: CyberStatusRole,
    val info: CyberStatusRole,
    val warning: CyberStatusRole,
    val caution: CyberStatusRole,
    val danger: CyberStatusRole
)

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
