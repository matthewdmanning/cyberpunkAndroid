package com.example.cyberpunkandroid.effects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.theme.CyberTheme

/*
 * ARCHITECTURE DIAGNOSTIC & ATTEMPTS LOG FOR GlowingText:
 * ----------------------------------------------------------------------------------
 * ATTEMPT 1: Modifier.blur() on Text composable.
 * - Result: FAILED. Compose's Modifier.blur() blurs the rectangular layer bounds of the Text node,
 *   producing a blurry rectangular shadow box around the text rather than glyph outline bloom.
 *
 * ATTEMPT 2: Native Canvas drawText with BlurMaskFilter(Blur.NORMAL).
 * - Result: FAILED. Android hardware acceleration (Skia/RenderThread) silently ignores
 *   Paint.setMaskFilter(BlurMaskFilter) on hardware Canvas calls, rendering no glow at all.
 *
 * ATTEMPT 3: Multi-Stage Font Glyph Shadow Layering (CURRENT VERIFIED SOLUTION).
 * - How it works: Layer 4 distinct Text passes in a Box with generous padding.
 *   - Pass 1 (Far Bloom): TextStyle with Shadow(blurRadius = 32dp, alpha = 0.25f)
 *   - Pass 2 (Mid Glow): TextStyle with Shadow(blurRadius = 16dp, alpha = 0.50f)
 *   - Pass 3 (Inner Glow): TextStyle with Shadow(blurRadius = 6dp, alpha = 0.85f)
 *   - Pass 4 (Foreground): Crisp sharp Text(textColor)
 * - Why this works: Native TextStyle Shadow calculates radial drop shadows strictly from font glyph vector paths.
 *   Layering 3 concentric shadow passes produces a 360-degree luminous aura surrounding each letter glyph with ZERO rectangular box!
 * ----------------------------------------------------------------------------------
 */

/**
 * Text composable with multi-stage font glyph bloom (emissive text glow following letter outlines with zero rectangular box boundaries).
 *
 * @param text Content text string.
 * @param modifier Composable modifier for text layout.
 * @param glowRadius Radius spread of the glyph bloom.
 * @param glowColor Tint color for the emissive glyph bloom.
 * @param textColor Tint color for the crisp foreground text layer.
 * @param fontSize Font size for the text layer.
 * @param appendedA11y Optional text to append to accessibility description.
 * @param customA11y Optional text to override accessibility description.
 */
@Composable
fun GlowingText(
    text: String,
    modifier: Modifier = Modifier,
    glowRadius: Dp = 16.dp,
    glowColor: Color = Color.Cyan,
    textColor: Color = Color.White,
    fontSize: TextUnit = 44.sp,
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    val density = LocalDensity.current
    val baseStyle = CyberTheme.typography.display.copy(
        fontSize = fontSize,
        fontWeight = FontWeight.Bold
    )

    val farBlurPx = with(density) { (glowRadius * 2f).toPx() }
    val midBlurPx = with(density) { glowRadius.toPx() }
    val innerBlurPx = with(density) { (glowRadius * 0.4f).toPx() }

    Box(
        modifier = modifier
            .cyberSemantics("GlowingText", appendedA11y, customA11y)
            .padding(glowRadius * 1.5f),
        contentAlignment = Alignment.Center
    ) {
        // Pass 1: Far Outer Bloom Shadow
        Text(
            text = text,
            color = glowColor.copy(alpha = 0.30f),
            style = baseStyle.copy(
                shadow = Shadow(
                    color = glowColor.copy(alpha = 0.60f),
                    offset = Offset.Zero,
                    blurRadius = farBlurPx
                )
            )
        )

        // Pass 2: Mid Glow Shadow
        Text(
            text = text,
            color = glowColor.copy(alpha = 0.60f),
            style = baseStyle.copy(
                shadow = Shadow(
                    color = glowColor.copy(alpha = 0.85f),
                    offset = Offset.Zero,
                    blurRadius = midBlurPx
                )
            )
        )

        // Pass 3: Inner High-Intensity Glow Shadow
        Text(
            text = text,
            color = glowColor,
            style = baseStyle.copy(
                shadow = Shadow(
                    color = glowColor,
                    offset = Offset.Zero,
                    blurRadius = innerBlurPx
                )
            )
        )

        // Pass 4: Crisp Sharp Foreground Text
        Text(
            text = text,
            color = textColor,
            style = baseStyle
        )
    }
}
