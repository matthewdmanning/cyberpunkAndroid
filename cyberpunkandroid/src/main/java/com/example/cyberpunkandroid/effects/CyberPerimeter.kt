package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.utils.CyberPathGeometry

/**
 * A [Shape] made of several filled parts, such as a thickened icon, that can list the
 * edges of those parts one contour at a time. [cyberPerimeter] runs a path effect along each.
 */
interface CyberPerimeterShape : Shape {
    /**
     * The closed edge contours for a component of [size], in px from its top-left corner, one
     * [Path] per contour. The outer edges of the filled parts come first. With [includeHoles],
     * the edges of holes inside them follow.
     */
    fun perimeters(size: Size, layoutDirection: LayoutDirection, density: Density, includeHoles: Boolean): List<Path>
}

/**
 * Draws a [CyberPathEffect] along the outside edge of a thickened shape, such as
 * [com.example.cyberpunkandroid.icons.CyberStrokeIcon], not along its centerline. Each filled
 * part gets its own loop of the effect, all driven by one animation.
 *
 * The effect is centered on the edge, so half of it lies over the shape and half outside;
 * the glow is padded so it isn't cut at the component bounds. Each loop starts in the middle
 * of its longest corner-free run, as in [cyberPathBorder].
 *
 * ```
 * val shape = CyberStrokeShape(centerline, width = 96.dp, thickness = 8.dp)
 * CyberStrokeIcon(shape, modifier = Modifier.cyberPerimeter(CyberCometTracer(), shape))
 * ```
 *
 * @param effect The path effect to draw.
 * @param shape The shape whose edges to follow. Pass the same shape the component draws.
 * @param color Stroke and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param glowRadius Blur radius of the glow. Zero disables glow.
 * @param includeHoles Also run the effect round the edges of holes, such as the inside of a ring.
 * @param steps When > 0, quantizes progress into this many steps per cycle.
 * @param trigger When the effect animates; while inactive it is drawn at progress 0 (or hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw nothing while the trigger is inactive.
 * @param animationSpec Drives progress 0 -> 1. Infinite specs loop; a finite spec plays once and holds.
 * @param appendedA11y Optional text appended to the default accessibility name.
 * @param customA11y Optional custom accessibility name.
 */
fun Modifier.cyberPerimeter(
    effect: CyberPathEffect,
    shape: CyberPerimeterShape,
    color: Color = Color.Unspecified,
    glowRadius: Dp = CyberPathDefaults.Modifiers.BorderGlow,
    includeHoles: Boolean = false,
    steps: Int = 0,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPathLoop,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberPerimeter", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val resolved = if (color == Color.Unspecified) CyberTheme.colors.primary else color
    val progress = rememberPathProgress(isActive, animationSpec)

    drawWithCache {
        // Contours shorter than a pixel (or not yet laid out) would give zero-length dash periods.
        val tracks = shape.perimeters(size, layoutDirection, this, includeHoles)
            .filter { CyberPathGeometry.length(it) >= 1f }
            .map { CyberPathGeometry.relocateSeam(it, this) }
        val renderers = tracks.map { effect.prepare(it, closed = true, density = this) }
        // One glow layer per loop: a layer holds one recording at a time.
        val glowLayers = tracks.map { obtainGraphicsLayer() }
        val glowPx = glowRadius.toPx()
        onDrawWithContent {
            drawContent()
            if (hideWhenIdle && !isActive) return@onDrawWithContent
            val p = quantize(progress.value, steps)
            for (i in tracks.indices) drawPathLayers(tracks[i], renderers[i].layers(p), resolved, glowPx, glowLayers[i])
        }
    }
}
