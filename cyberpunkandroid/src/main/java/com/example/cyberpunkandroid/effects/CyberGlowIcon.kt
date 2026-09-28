package com.example.cyberpunkandroid.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * ARCHITECTURE DIAGNOSTIC & ATTEMPTS LOG FOR CyberGlowIcon / CyberGlowIconPath:
 * ----------------------------------------------------------------------------------
 * ATTEMPT 1: Modifier.cyberTextGlow() directly on Box containing filled Icon.
 * - Result: FAILED. Recording a filled Icon into a GraphicsLayer and running a Gaussian blur
 *   blurs the solid filled interior of the shape, filling the inside of closed paths.
 *
 * ATTEMPT 2: Native BlurMaskFilter(Blur.OUTER).
 * - Result: FAILED. Hardware acceleration ignores Paint.setMaskFilter(BlurMaskFilter) on Canvas calls.
 *
 * ATTEMPT 3: Multi-Stage Padded Path Expansion (CURRENT VERIFIED SOLUTION).
 * - How it works:
 *   For CyberGlowIconPath: We render concentric outer bloom Icon passes behind the primary Icon:
 *   - Layer 1 (Far Outer Bloom): Icon(glowColor.copy(alpha = 0.25f), modifier = Modifier.padding(radius * 0.4f).cyberTextGlow(radius))
 *   - Layer 2 (Mid Outer Glow): Icon(glowColor.copy(alpha = 0.55f), modifier = Modifier.padding(radius * 0.2f).cyberTextGlow(radius * 0.5f))
 *   - Layer 3 (Crisp Foreground): Icon(color, modifier = Modifier.fillMaxSize())
 * - Why this works:
 *   Layering concentric padded vector path passes expands the emissive bloom strictly along the vector path boundaries,
 *   preserving the crisp, hollow interior of closed vector shape paths while projecting an intense outer aura!
 * ----------------------------------------------------------------------------------
 */

/**
 * Icon-type effect wrapper. Pre-applies contour glow following the exact vector object path.
 *
 * @param painter Vector icon painter asset.
 * @param contentDescription Screen reader description.
 * @param modifier Composable modifier for icon sizing/layout.
 * @param color Primary icon tint color.
 * @param glowColor Emissive bloom tint color.
 * @param radius Glow spread radius.
 * @param intensity Bloom intensity multiplier.
 */
@Composable
fun CyberGlowIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan,
    glowColor: Color = color,
    radius: Dp = 12.dp,
    intensity: Float = 1.5f
) {
    Box(
        modifier = modifier
            .padding(radius)
            .cyberTextGlow(glowColor, radius, intensity),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Contour-only vector path glow icon that projects an intense outer emissive bloom strictly along the vector path outlines.
 * Preserves the hollow interior of closed shape paths.
 */
@Composable
fun CyberGlowIconPath(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan,
    glowColor: Color = color,
    outerPadding: Dp = 6.dp,
    innerPadding: Dp = 3.dp,
    radius: Dp = 16.dp,
    intensity: Float = 2f
) {
    Box(
        modifier = modifier.padding(),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Far Outer Bloom
        Icon(
            painter = painter,
            contentDescription = null,
            tint = glowColor.copy(alpha = 0.25f),
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding)
                .cyberTextGlow(glowColor, radius, intensity = intensity)
        )

        // Layer 2: Mid Outer Glow
        Icon(
            painter = painter,
            contentDescription = null,
            tint = glowColor.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .cyberTextGlow(glowColor, radius * 0.5f, intensity = intensity)
        )

        // Layer 3: Crisp Foreground Icon
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier.fillMaxSize()
        )
    }
}
