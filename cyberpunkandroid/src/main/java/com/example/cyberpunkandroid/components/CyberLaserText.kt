package com.example.cyberpunkandroid.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.effects.CyberInteractionTrigger
import com.example.cyberpunkandroid.effects.cyberLaserOutliner
import com.example.cyberpunkandroid.effects.cyberSemantics

/**
 * Text composable with a dynamic Laser Outliner effect: a glowing laser beam welds each letter's
 * border into existence from left to right, one letter at a time, complete with physics-driven sparks,
 * molten glow, and cooling metal transition.
 *
 * Fires once on entrance by default, holding the finished welded outline in place.
 *
 * @param text The text string to weld.
 * @param modifier Composable layout modifier.
 * @param fontSize Typography size of the welded font glyphs.
 * @param strokeWidth Thickness of the laser ray and welded outline stroke.
 * @param glowRadius Radius of the optical halo surrounding the laser and molten seam.
 * @param sparkCount Number of fizzing weld sparks emitted from the contact point.
 * @param durationMillis Duration in milliseconds for the complete left-to-right pass.
 * @param laserColor Color of the piercing vertical laser beam and contact flare.
 * @param weldColor Hot molten color of the freshly deposited weld pool.
 * @param coolColor Color of the final cooled and settled weld outline. Defaults to `CyberTheme.colors.primary`.
 * @param guideAlpha Opacity of the unwelded blueprint guide outline.
 * @param trigger When the laser welds; defaults to [CyberInteractionTrigger.ALWAYS].
 * @param interactionSource Optional source for interaction-triggered re-welding.
 * @param animationSpec Drives progress 0 -> 1. Defaults to a finite one-shot `tween`.
 * @param appendedA11y Text appended to the accessibility description.
 * @param customA11y Complete override for the accessibility description.
 */
@Composable
fun CyberLaserText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    strokeWidth: Dp = 1.5.dp,
    glowRadius: Dp = 8.dp,
    sparkCount: Int = 10,
    durationMillis: Int = 2400,
    laserColor: Color = Color.Cyan,
    weldColor: Color = Color(0xFFFFB800),
    coolColor: Color = Color.Unspecified,
    guideAlpha: Float = 0.08f,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    animationSpec: AnimationSpec<Float> = tween(durationMillis, easing = LinearEasing),
    appendedA11y: String? = null,
    customA11y: String? = null
) {
    val density = LocalDensity.current

    // Estimate natural dimensions for the text to size the container comfortably
    val (estimatedWidthDp, estimatedHeightDp) = remember(text, fontSize, density) {
        val paint = Paint().apply {
            this.textSize = with(density) { fontSize.toPx() }
            this.typeface = Typeface.MONOSPACE
            this.isAntiAlias = true
        }
        val widths = FloatArray(text.length)
        paint.getTextWidths(text, widths)
        val textWidthPx = widths.sum()
        val metrics = paint.fontMetrics
        val textHeightPx = (metrics.descent - metrics.ascent) * 1.6f // room for laser descent and sparks

        with(density) {
            Pair(textWidthPx.toDp() + 24.dp, textHeightPx.toDp())
        }
    }

    Box(
        modifier = modifier
            .cyberSemantics("CyberLaserText", appendedA11y, customA11y)
            .defaultMinSize(minWidth = estimatedWidthDp, minHeight = estimatedHeightDp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .cyberLaserOutliner(
                text = text,
                fontSize = fontSize,
                laserColor = laserColor,
                weldColor = weldColor,
                coolColor = coolColor,
                guideAlpha = guideAlpha,
                strokeWidth = strokeWidth,
                glowRadius = glowRadius,
                sparkCount = sparkCount,
                durationMillis = durationMillis,
                trigger = trigger,
                interactionSource = interactionSource,
                animationSpec = animationSpec
            )
    )
}
