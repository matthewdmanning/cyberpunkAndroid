package com.example.cyberpunkandroid.effects

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density

/**
 * One `drawPath` call produced by a [CyberPathEffect] for a given animation progress.
 *
 * Effects return several layers instead of a `SumPathEffect` (which is not part of Compose's
 * common PathEffect API): a dim track, a fading tail, a hot head, and so on.
 *
 * @param pathEffect Geometry effect for this pass, or null to stroke the outline as-is.
 * @param width Stroke width in px. Stamped effects are filled, so their width is ignored.
 * @param cap Stroke cap for dash ends.
 * @param alpha Opacity of this pass (0..1).
 * @param hot 0..1 mix toward white, for the overexposed core of a neon tracer head.
 * @param glow Whether this pass also feeds the blurred glow layer.
 * @param path Replacement outline for this pass (stamped patterns animate by rotating the
 *   contour start; sparks are free-standing strokes), or null to draw on the prepared outline.
 * @param color Color for this pass, or `Color.Unspecified` to use the modifier's color.
 */
@Immutable
class CyberPathLayer(
    val pathEffect: PathEffect?,
    val width: Float,
    val cap: StrokeCap = StrokeCap.Butt,
    val alpha: Float = 1f,
    val hot: Float = 0f,
    val glow: Boolean = true,
    val path: Path? = null,
    val color: Color = Color.Unspecified,
)

/** A [CyberPathEffect] resolved against one concrete outline. */
fun interface CyberPathRenderer {
    /** Layers to draw at [progress] (0..1, one animation cycle). */
    fun layers(progress: Float): List<CyberPathLayer>
}

/**
 * A path effect for borders and dividers: geometry (dashes, stamps, jitter) plus the
 * choreography that animates it. Implementations are data classes so they compare by value.
 *
 * Apply with [cyberPathBorder] or [cyberPathDivider].
 */
@Stable
interface CyberPathEffect {
    /** How far the effect's geometry reaches from the outline, in px; borders inset by this so nothing clips. */
    fun extent(density: Density): Float

    /**
     * Resolves the effect for one outline. Called once per outline/size (inside `drawWithCache`),
     * so measuring and stamp building happen here, not per frame.
     *
     * @param outline Outline to decorate; closed outlines already have their seam moved off corners.
     * @param closed Whether the outline is a closed contour (borders) or an open line (dividers).
     */
    fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer
}
