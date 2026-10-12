package com.example.cyberpunkandroid.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPrimitives

@Immutable
data class CyberSpacing(
    val dp2: Dp = CyberPrimitives.Spacing.dp2,
    val dp4: Dp = CyberPrimitives.Spacing.dp4,
    val dp8: Dp = CyberPrimitives.Spacing.dp8,
    val dp12: Dp = CyberPrimitives.Spacing.dp12,
    val dp16: Dp = CyberPrimitives.Spacing.dp16,
    val dp24: Dp = CyberPrimitives.Spacing.dp24,
    val dp32: Dp = CyberPrimitives.Spacing.dp32,
    val dp48: Dp = CyberPrimitives.Spacing.dp48,
    val dp64: Dp = CyberPrimitives.Spacing.dp64
)

val LocalCyberSpacing = staticCompositionLocalOf { CyberSpacing() }
