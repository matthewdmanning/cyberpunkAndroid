package com.example.cyberpunkandroid.effects

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/** Default loop for path effects: one cycle every 2.4 s, constant speed. */
private val DefaultPathLoop: AnimationSpec<Float> =
    infiniteRepeatable(animation = tween(2400, easing = LinearEasing), repeatMode = RepeatMode.Restart)

/**
 * Draws a [CyberPathEffect] along the component's [shape] outline, over its content, with a neon glow.
 *
 * Box patterns (for example [CyberHazardBand], [CyberBraid]) march along the outline while
 * active; tracers (for example [CyberCometTracer], [CyberCornerCharge]) run their light
 * choreography once per animation cycle. With `trigger = CyberInteractionTrigger.NONE` the effect
 * is drawn static at progress 0.
 *
 * ### Rendering
 * - Geometry comes from Compose `PathEffect`s (dash, stamped, chained) and Android's `DiscretePathEffect`.
 * - **Android 12+ (API 31+):** glow passes are drawn at double width into a `GraphicsLayer` blurred with
 *   `RenderEffect.createBlurEffect`, padded so the glow is not cut at the component bounds.
 * - **API < 31:** no blur is available, so glow falls back to two widened translucent strokes.
 * - The outline is inset by the effect's extent (or [inset]) so stroke geometry stays inside the bounds,
 *   and its start is moved to the middle of the longest side so pattern seams never sit on a corner.
 *
 * @param effect The path effect to draw.
 * @param color Stroke and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param shape Outline to follow; match the component's background/clip shape.
 * @param glowRadius Blur radius of the glow. Zero disables glow.
 * @param inset Outline inset; `Dp.Unspecified` uses the effect's own extent.
 * @param steps When > 0, quantizes progress into this many steps per cycle (stepped, HUD-like motion).
 * @param trigger When the effect animates; while inactive it is drawn at progress 0 (or hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw nothing while the trigger is inactive (for tracers that should only run on interaction).
 * @param animationSpec Drives progress 0 -> 1. Infinite specs loop; a finite spec plays once and holds.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
  * @param customA11y
)

/** [cyberPathBorder] with a custom accessibility name, for effect-specific shortcuts such as [cyberWeld]. */
internal fun Modifier.cyberPathBorderNamed(
    a11yName TODO: document this
 */
fun Modifier.cyberPathBorder(
    effect: CyberPathEffect,
    color: Color = Color.Unspecified,
    shape: Shape = RectangleShape,
    glowRadius: Dp = CyberPathDefaults.Modifiers.BorderGlow,
    inset: Dp = Dp.Unspecified,
    steps: Int = 0,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPathLoop,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberPathBorderNamed(
    "CyberPathBorder", effect, color, shape, glowRadius, inset, steps, trigger, interactionSource, hideWhenIdle, animationSpec, appendedA11y, customA11y
)

/**
 * [cyberPathBorder] with a custom accessibility name, for effect-specific shortcuts such as [cyberWeld].
 * @param a11yName TODO: document this
 * @param color TODO: document this
 * @param glowRadius TODO: document this
 * @param inset TODO: document this
 * @param steps TODO: document this
 * @param hideWhenIdle TODO: document this
 * @param appendedA11y TODO: document this
 * @param customA11y TODO: document this
 */
internal fun Modifier.cyberPathBorderNamed(
    a11yName: String,
    effect: CyberPathEffect,
    color: Color = Color.Unspecified,
    shape: Shape = RectangleShape,
    glowRadius: Dp = CyberPathDefaults.Modifiers.BorderGlow,
    inset: Dp = Dp.Unspecified,
    steps: Int = 0,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPathLoop,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics(a11yName, appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val resolved = if (color == Color.Unspecified) CyberTheme.colors.primary else color
    val progress = rememberPathProgress(isActive, animationSpec)

    drawWithCache {
        val insetPx = if (inset.value.isNaN()) effect.extent(this) else inset.toPx() // Dp.Unspecified -> effect extent
        val raw = CyberPathGeometry.outlinePath(shape, size, layoutDirection, this, insetPx)
        val outline = CyberPathGeometry.relocateSeam(raw, this)
        // sub-pixel outlines (not yet laid out) would give zero-length dash/stamp periods
        val renderer = if (CyberPathGeometry.length(outline) < 1f) CyberPathRenderer { emptyList() }
        else effect.prepare(outline, closed = true, density = this)
        val glowLayer = obtainGraphicsLayer()
        val glowPx = glowRadius.toPx()
        onDrawWithContent {
            drawContent()
            if (hideWhenIdle && !isActive) return@onDrawWithContent
            val layers = renderer.layers(quantize(progress.value, steps))
            drawPathLayers(outline, layers, resolved, glowPx, glowLayer)
        }
    }
}

/**
 * Draws a [CyberPathEffect] along a horizontal line across the component: a glowing divider.
 * Scanner, comet, live wire and packet effects work especially well here.
 *
 * @param effect The path effect to draw.
 * @param color Stroke and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param alignment Vertical position of the line within the component.
 * @param horizontalInset Space left free at both ends of the line.
 * @param glowRadius Blur radius of the glow. Zero disables glow.
 * @param steps When > 0, quantizes progress into this many steps per cycle.
 * @param trigger When the effect animates; while inactive it is drawn at progress 0 (or hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw nothing while the trigger is inactive.
 * @param animationSpec Drives progress 0 -> 1. Infinite specs loop; a finite spec plays once and holds.
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
fun Modifier.cyberPathDivider(
    effect: CyberPathEffect,
    color: Color = Color.Unspecified,
    alignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalInset: Dp = CyberPathDefaults.Modifiers.DividerInset,
    glowRadius: Dp = CyberPathDefaults.Modifiers.DividerGlow,
    steps: Int = 0,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPathLoop,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberPathDivider", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val resolved = if (color == Color.Unspecified) CyberTheme.colors.primary else color
    val progress = rememberPathProgress(isActive, animationSpec)

    drawWithCache {
        val y = alignment.align(0, size.height.roundToInt()).toFloat()
        val x0 = horizontalInset.toPx()
        val line = Path().apply {
            moveTo(x0, y)
            lineTo((size.width - x0).coerceAtLeast(x0 + 1f), y)
        }
        val renderer = if (size.width < 2f * x0 + 1f) CyberPathRenderer { emptyList() } // too narrow to draw a line
        else effect.prepare(line, closed = false, density = this)
        val glowLayer = obtainGraphicsLayer()
        val glowPx = glowRadius.toPx()
        onDrawWithContent {
            drawContent()
            if (hideWhenIdle && !isActive) return@onDrawWithContent
            drawPathLayers(line, renderer.layers(quantize(progress.value, steps)), resolved, glowPx, glowLayer)
        }
    }
}

/**
 * Draws a [CyberPathEffect] along a path you supply: the way to run a tracer or a particle shower along an arc,
 * a circle, a spiral or any other line that is not the component's outline or a straight divider.
 *
 * The path is built once per size, so build it from the size you are given; it is drawn over the content with
 * the same neon glow as [cyberPathBorder]. A closed path loops the effect round it; an open path has a start and an end.
 *
 * ```
 * Modifier.size(160.dp).cyberPathAlong(CyberParticleShower(), path = { size ->
 *     Path().apply { addArc(Rect(Offset.Zero, size.minDimension / 2f), 150f, 240f) }  // open arc, centered
 * })
 * ```
 *
 * @param effect The path effect to draw.
 * @param path Builds the path for a component of the given size, in pixels from the component's top-left corner.
 *   It is a `Density` extension so you can convert `Dp` values with `toPx()`.
 * @param closed Whether [path] is a closed contour (the effect loops round it) or an open line.
 * @param color Stroke and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param glowRadius Blur radius of the glow. Zero disables glow.
 * @param steps When > 0, quantizes progress into this many steps per cycle.
 * @param trigger When the effect animates; while inactive it is drawn at progress 0 (or hidden, see [hideWhenIdle]).
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw nothing while the trigger is inactive.
 * @param animationSpec Drives progress 0 -> 1. Infinite specs loop; a finite spec plays once and holds.
 */
fun Modifier.cyberPathAlong(
    effect: CyberPathEffect,
    path: Density.(size: Size) -> Path,
    closed: Boolean = false,
    color: Color = Color.Unspecified,
    glowRadius: Dp = CyberPathDefaults.Modifiers.BorderGlow,
    steps: Int = 0,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = DefaultPathLoop,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberPathAlong", appendedA11y, customA11y).composed {
    val isActive = trigger.isActive(interactionSource)
    val resolved = if (color == Color.Unspecified) CyberTheme.colors.primary else color
    val progress = rememberPathProgress(isActive, animationSpec)

    drawWithCache {
        val line = path(this, size)
        // a path shorter than a pixel (or not yet laid out) would give zero-length dash periods
        val renderer = if (CyberPathGeometry.length(line) < 1f) CyberPathRenderer { emptyList() }
        else effect.prepare(line, closed, this)
        val glowLayer = obtainGraphicsLayer()
        val glowPx = glowRadius.toPx()
        onDrawWithContent {
            drawContent()
            if (hideWhenIdle && !isActive) return@onDrawWithContent
            drawPathLayers(line, renderer.layers(quantize(progress.value, steps)), resolved, glowPx, glowLayer)
        }
    }
}

/**
 * Progress 0 -> 1 driven by [animationSpec] while [isActive]; snaps back to 0 when inactive.
 * @param isActive TODO: document this
 */
@Composable
internal fun rememberPathProgress(isActive: Boolean, animationSpec: AnimationSpec<Float>): State<Float> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(isActive, animationSpec) {
        progress.snapTo(0f)
        if (isActive) progress.animateTo(1f, animationSpec)
    }
    return progress.asState()
}

internal fun quantize(p: Float, steps: Int): Float = if (steps > 0) floor(p * steps) / steps else p

/** The layer's own color, or the modifier color when the layer doesn't set one. */
private fun CyberPathLayer.baseColor(modifierColor: Color): Color = if (color == Color.Unspecified) modifierColor else color

/**
 * Draws [layers] along [outline]: glow passes first (blurred on API 31+, widened strokes below),
 * then the crisp passes. Each layer's `hot` mixes its color toward white.
  * @param color TODO: document this
  * @param glowRadiusPx TODO: document this
 */
internal fun DrawScope.drawPathLayers(
    outline: Path,
    layers: List<CyberPathLayer>,
    color: Color,
    glowRadiusPx: Float,
    glowLayer: GraphicsLayer,
) {
    val glowing = layers.filter { it.glow && it.alpha > 0.003f }
    if (glowRadiusPx > 0f && glowing.isNotEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val pad = ceil(glowRadiusPx * 3f).toInt() // room for the blur tail outside the bounds
            glowLayer.renderEffect = android.graphics.RenderEffect.createBlurEffect(
                glowRadiusPx, glowRadiusPx, android.graphics.Shader.TileMode.DECAL
            ).asComposeRenderEffect()
            glowLayer.topLeft = IntOffset(-pad, -pad)
            glowLayer.record(this, layoutDirection, IntSize(size.width.toInt() + 2 * pad, size.height.toInt() + 2 * pad)) {
                translate(pad.toFloat(), pad.toFloat()) {
                    for (l in glowing) {
                        drawPath(
                            path = l.path ?: outline,
                            color = lerp(l.baseColor(color), Color.White, l.hot * 0.4f), // glow stays colored; only the core goes white
                            alpha = l.alpha.coerceIn(0f, 1f),
                            style = Stroke(width = l.width * 2f, cap = l.cap, pathEffect = l.pathEffect),
                        )
                    }
                }
            }
            drawLayer(glowLayer)
        } else {
            for (l in glowing) {
                // two widened translucent strokes approximate a blur without RenderEffect
                val c = lerp(l.baseColor(color), Color.White, l.hot * 0.4f)
                drawPath(l.path ?: outline, c, alpha = (l.alpha * 0.18f).coerceIn(0f, 1f),
                    style = Stroke(width = l.width * 2f + glowRadiusPx, cap = l.cap, pathEffect = l.pathEffect))
                drawPath(l.path ?: outline, c, alpha = (l.alpha * 0.30f).coerceIn(0f, 1f),
                    style = Stroke(width = l.width * 2f, cap = l.cap, pathEffect = l.pathEffect))
            }
        }
    }
    for (l in layers) {
        if (l.alpha <= 0.003f) continue
        drawPath(
            path = l.path ?: outline,
            color = lerp(l.baseColor(color), Color.White, l.hot),
            alpha = l.alpha.coerceIn(0f, 1f),
            style = Stroke(width = l.width, cap = l.cap, pathEffect = l.pathEffect),
        )
    }
}
