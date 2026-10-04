package com.example.cyberpunkandroid.effects

import com.example.cyberpunkandroid.utils.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.toSize
import com.example.cyberpunkandroid.config.CyberRadialDefaults
import com.example.cyberpunkandroid.theme.CyberElevation
import com.example.cyberpunkandroid.theme.CyberTheme
import com.example.cyberpunkandroid.utils.CyberPathGeometry

// The three radial modifiers. Each one only DRAWS (or lights); timing, angles and fade belong to the hoisted
// driver (CyberRadarSweep / CyberRadialPulse in CyberRadial.kt), and the geometry comes from CyberRadialMath, so
// the wedge, the ring and the icon glow all agree on where the beam is.

/**
 * Draws a radar sweep over the component: a rotating beam with a fading wedge behind it.
 *
 * ### Appearance
 * - A bright, slightly over-exposed line from the inner to the outer radius marks the head of the beam; it glows
 *   (blurred on Android 12+, two widened strokes below).
 * - Behind the head the wedge fades from [wedgeAlpha] to transparent over the driver's `tailDegrees`; with
 *   `fadeSteps > 0` the fade is cut into visibly discrete bands. Ahead of the head nothing is drawn.
 * - A partial sector clips the wedge to a pie slice with a hard edge.
 * - The effect is drawn over the content, so it also tints the background. To light only icons, set
 *   `showWedge = false`, `headWidth = CyberElevation.level0` and put [cyberRadialIllumination] on the icons.
 *
 * ```
 * val sweep = rememberCyberRadarSweep(tailDegrees = 120f, fadeSteps = 6)
 * Box(Modifier.size(200.dp).background(CyberTheme.colors.background, CircleShape).cyberRadarSweep(sweep, region = CyberRadialRegion(clipShape = CircleShape))) { ... }
 * ```
 *
 * TODO: spatially feathered sector edges (a soft fade across the slice boundary instead of a hard cut). A hard-edged
 *  clip is the only version that holds up at every size without visible banding, so it ships first.
 *
 * Applying it publishes the effect's position to [sweep], which is how [cyberRadialIllumination] knows where it is.
 * The modifier is decorative: it adds no accessibility node unless [appendedA11y] or [customA11y] is given.
 *
 * @param sweep The driver: sector, mode, direction, trail, fade and the animation clock.
 * @param color Wedge, head and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param region Origin, radii and clipping of the effect. Defaults to the component center, reaching the farthest corner, clipped to the bounds.
 * @param wedgeAlpha Opacity of the wedge at its head, 0..1.
 * @param headWidth Thickness of the head line. Zero hides it.
 * @param glowRadius Blur radius of the head line's glow. Zero disables the glow.
 * @param showWedge Draw the fading wedge. False leaves only the head line (a line-only radar).
 * @param appendedA11y Text appended to the accessibility name; gives the modifier a semantics node.
 * @param customA11y Replaces the accessibility name; gives the modifier a semantics node.
 */
fun Modifier.cyberRadarSweep(
    sweep: CyberRadarSweep,
    color: Color = Color.Unspecified,
    region: CyberRadialRegion = CyberRadialRegion(),
    wedgeAlpha: Float = CyberRadialDefaults.Sweep.WedgeAlpha,
    headWidth: Dp = CyberRadialDefaults.Sweep.HeadWidth,
    glowRadius: Dp = CyberRadialDefaults.Sweep.GlowRadius,
    showWedge: Boolean = true,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .radialSemantics("CyberRadarSweep", appendedA11y, customA11y)
    .trackRadialPlacement(region) { sweep.placement = it }
    .composed {
        val resolvedColor = if (color == Color.Unspecified) CyberTheme.colors.primary else color
        drawWithCache {
            val clip = region.clipShape?.let { CyberPathGeometry.outlinePath(it, size, layoutDirection, this, 0f) }
            val glowLayer = obtainGraphicsLayer()
            val glowPx = glowRadius.toPx()
            val headPx = headWidth.toPx()
            val headLine = Path()
            val wedges = WedgeCache()
            val pie = PieCache()
            onDrawWithContent {
                drawContent()
                if (sweep.hideWhenIdle && !sweep.isActive) return@onDrawWithContent
                val area = region.resolve(size, this)
                if (area.outerPx - area.innerPx < 1f) return@onDrawWithContent // nothing to draw in an empty ring
                val pose = sweep.pose()
                val sector = sweep.sector
                val fade = CyberRadialMath.edgeFactor(pose.offset, sector.span, sweep.edgeFadeDegrees)
                val headDegrees = sector.startDegrees + pose.offset
                val trailDegrees = sweep.tailDegrees * pose.tailScale
                clipTo(clip) {
                    if (showWedge && wedgeAlpha > 0f && trailDegrees > 0f) {
                        val wedge = wedges.get(trailDegrees, sweep.fadeSteps, pose.movingClockwise, resolvedColor, wedgeAlpha)
                        translate(area.origin.x, area.origin.y) {
                            clipTo(if (sector.isFullCircle) null else pie.get(sector, area.outerPx)) {
                                // The gradient is built with the head at wedge.headFraction; turn it so that lands on the head's real angle.
                                val turn = CyberRadialMath.toComposeDegrees(headDegrees) - wedge.headFraction * CyberRadialMath.FULL_TURN
                                rotate(turn, pivot = Offset.Zero) {
                                    drawRingFill(wedge.brush, fade, area.innerPx, area.outerPx)
                                }
                            }
                        }
                    }
                    if (headPx > 0f && pose.offset in 0f..sector.span) { // a wrapping beam that has left the sector has no head to draw
                        headLine.rewind()
                        headLine.moveTo(
                            CyberRadialMath.polarX(area.origin.x, area.innerPx, headDegrees),
                            CyberRadialMath.polarY(area.origin.y, area.innerPx, headDegrees),
                        )
                        headLine.lineTo(
                            CyberRadialMath.polarX(area.origin.x, area.outerPx, headDegrees),
                            CyberRadialMath.polarY(area.origin.y, area.outerPx, headDegrees),
                        )
                        val layer = CyberPathLayer(null, headPx, StrokeCap.Butt, fade, CyberRadialDefaults.Sweep.HeadHot)
                        drawPathLayers(headLine, listOf(layer), resolvedColor, glowPx, glowLayer)
                    }
                }
            }
        }
    }

/**
 * Draws a radial pulse over the component: rings or a filled disc travelling between the inner and outer radius.
 *
 * ### Appearance
 * - [CyberPulseStyle.RING]: one thin glowing ring with a soft trail behind it.
 * - [CyberPulseStyle.DISC]: the same ring, with the area it has already crossed filled at the driver's `discFill` opacity.
 * - [CyberPulseStyle.SONAR]: several evenly staggered rings in flight at once, each fading out as it nears its end radius.
 * - The direction is outward (inner to outer) or inward (outer to inner). A partial sector draws arcs and a fan instead of full circles.
 * - With `fadeSteps > 0` the trail is cut into visibly discrete bands.
 *
 * ```
 * val pulse = rememberCyberRadialPulse(style = CyberPulseStyle.SONAR, direction = CyberRadialDirection.OUTWARD)
 * Box(Modifier.size(200.dp).cyberRadialPulse(pulse, region = CyberRadialRegion(clipShape = CircleShape)))
 * ```
 *
 * TODO: spatially feathered sector edges, as for [cyberRadarSweep].
 *
 * Applying it publishes the effect's position to [pulse], so [cyberRadialIllumination] can light icons as the rings pass.
 * The modifier is decorative: it adds no accessibility node unless [appendedA11y] or [customA11y] is given.
 *
 * @param pulse The driver: style, direction, sector, trail and the animation clock.
 * @param color Ring, trail and glow color. Defaults to `CyberTheme.colors.primary`.
 * @param region Origin, radii and clipping of the effect. Defaults to the component center, reaching the farthest corner, clipped to the bounds.
 * @param trailAlpha Opacity of the trail at the ring head, 0..1.
 * @param ringWidth Thickness of the ring line. Zero hides it.
 * @param glowRadius Blur radius of the ring's glow. Zero disables the glow.
 * @param appendedA11y Text appended to the accessibility name; gives the modifier a semantics node.
 * @param customA11y Replaces the accessibility name; gives the modifier a semantics node.
 */
fun Modifier.cyberRadialPulse(
    pulse: CyberRadialPulse,
    color: Color = Color.Unspecified,
    region: CyberRadialRegion = CyberRadialRegion(),
    trailAlpha: Float = CyberRadialDefaults.Pulse.TrailAlpha,
    ringWidth: Dp = CyberRadialDefaults.Pulse.RingWidth,
    glowRadius: Dp = CyberRadialDefaults.Pulse.GlowRadius,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .radialSemantics("CyberRadialPulse", appendedA11y, customA11y)
    .trackRadialPlacement(region) { pulse.placement = it }
    .composed {
        val resolvedColor = if (color == Color.Unspecified) CyberTheme.colors.primary else color
        drawWithCache {
            val clip = region.clipShape?.let { CyberPathGeometry.outlinePath(it, size, layoutDirection, this, 0f) }
            val glowLayer = obtainGraphicsLayer()
            val glowPx = glowRadius.toPx()
            val ringPx = ringWidth.toPx()
            val trailPx = pulse.trail.toPx()
            val ringPaths = List(pulse.rings) { Path() } // one path per ring, reused every frame
            val pie = PieCache()
            onDrawWithContent {
                drawContent()
                if (pulse.hideWhenIdle && !pulse.isActive) return@onDrawWithContent
                val area = region.resolve(size, this)
                val travel = area.outerPx - area.innerPx
                if (travel < 1f) return@onDrawWithContent // under a pixel of travel: nothing to draw, and the gradient radius must stay positive
                val sector = pulse.sector
                val outward = pulse.direction == CyberRadialDirection.OUTWARD
                val ringLayers = ArrayList<CyberPathLayer>(pulse.rings)
                clipTo(clip) {
                    for (ring in 0 until pulse.rings) {
                        val life = pulse.lifeProgress(ring)
                        val fade = CyberRadialMath.pulseFade(life, pulse.fadeStart)
                        if (fade <= 0f) continue
                        val headRadius = area.innerPx + CyberRadialMath.pulseHead(life, pulse.direction) * travel
                        if (trailPx > 0f || pulse.fill > 0f) {
                            val stops = CyberRadialMath.trailStops(headRadius / area.outerPx, trailPx / area.outerPx, pulse.fadeSteps, pulse.fill, behindIsLower = outward)
                            val brush = Brush.radialGradient(
                                *stops.map { it.position to resolvedColor.copy(alpha = it.alpha * trailAlpha) }.toTypedArray(),
                                center = Offset.Zero,
                                radius = area.outerPx,
                            )
                            translate(area.origin.x, area.origin.y) {
                                clipTo(if (sector.isFullCircle) null else pie.get(sector, area.outerPx)) {
                                    drawRingFill(brush, fade, area.innerPx, area.outerPx)
                                }
                            }
                        }
                        if (ringPx > 0f && headRadius > 0f) {
                            val path = ringPaths[ring]
                            path.rewind()
                            val oval = Rect(area.origin, headRadius)
                            if (sector.isFullCircle) path.addOval(oval)
                            else path.addArc(oval, CyberRadialMath.toComposeDegrees(sector.startDegrees), sector.span)
                            ringLayers.add(CyberPathLayer(null, ringPx, StrokeCap.Butt, fade, CyberRadialDefaults.Pulse.RingHot, path = path))
                        }
                    }
                    if (ringLayers.isNotEmpty()) drawPathLayers(ringPaths[0], ringLayers, resolvedColor, glowPx, glowLayer)
                }
            }
        }
    }

/**
 * Lights the component's content, such as an icon or text, while a radial effect passes over it. Only the content
 * glows; nothing is drawn on the background behind it.
 *
 * ### Appearance
 * A blurred, recolored copy of the content's silhouette appears behind it (the same contour glow as `cyberTextGlow`).
 * Its strength follows the driver: full as the beam head or ring passes the component's center, then dying away
 * with the driver's trail, in discrete bands when the driver uses `fadeSteps`.
 *
 * The component is lit as a whole, by the effect's brightness at its center. Where the center sits outside the
 * effect's radii or sector, it stays dark.
 *
 * ```
 * val sweep = rememberCyberRadarSweep()
 * Box(Modifier.size(200.dp).cyberRadarSweep(sweep, showWedge = false, headWidth = CyberElevation.level0)) {
 *     CyberIcon(CyberIcons.Wifi, "Wifi", Modifier.align(Alignment.TopCenter).cyberRadialIllumination(sweep))
 * }
 * ```
 *
 * TODO: nearby spill. Light the neighbours of a lit icon, with a soft halo on the background, as a second mode
 *  next to this contour glow. It needs a tuned falloff that can only be judged on a device.
 * TODO: steady glow. Hold a lit icon at a base level between passes, and a mode that keeps the glow
 *  on while the icon is under the beam instead of following the wedge.
 *
 * The modifier is decorative: it keeps the content's own accessibility description and adds a node only when
 * [appendedA11y] or [customA11y] is given.
 *
 * @param field What lights the content: a [CyberRadarSweep], a [CyberRadialPulse], or your own [CyberRadialField].
 * It must be drawn by `cyberRadarSweep` or `cyberRadialPulse` in the same window to know where it is.
 * @param color Glow color. Defaults to `CyberTheme.colors.primary`.
 * @param radius Blur radius of the glow.
 * @param maxIntensity Glow strength at full brightness; 1 is the standard `cyberTextGlow` strength.
 * @param appendedA11y Text appended to the accessibility name; gives the modifier a semantics node.
 * @param customA11y Replaces the accessibility name; gives the modifier a semantics node.
 */
fun Modifier.cyberRadialIllumination(
    field: CyberRadialField,
    color: Color = Color.Unspecified,
    radius: Dp = CyberRadialDefaults.Illumination.Radius,
    maxIntensity: Float = CyberRadialDefaults.Illumination.MaxIntensity,
    appendedA11y: String? = null,
    customA11y: String? = null,
): Modifier = this
    .radialSemantics("CyberRadialIllumination", appendedA11y, customA11y)
    .composed {
        val resolvedColor = if (color == Color.Unspecified) CyberTheme.colors.primary else color
        var bounds: Rect? by remember { mutableStateOf<Rect?>(null) } // component bounds in root coordinates
        Modifier
            .onGloballyPositioned { bounds = Rect(it.positionInRoot(), it.size.toSize()) }
            .drawWithCache {
                val glowLayer = obtainGraphicsLayer()
                val blurPx = radius.toPx()
                onDrawWithContent {
                    val b = bounds
                    val level = if (b == null) 0f else {
                        val c = field.intensityAt(b.center)
                        val tl = field.intensityAt(b.topLeft)
                        val tr = field.intensityAt(b.topRight)
                        val bl = field.intensityAt(b.bottomLeft)
                        val br = field.intensityAt(b.bottomRight)
                        maxOf(c, tl, tr, bl, br).coerceIn(0f, 1f) * maxIntensity
                    }
                    if (level >= CyberRadialDefaults.Illumination.MinVisible) drawContourGlow(glowLayer, resolvedColor, blurPx, level)
                    drawContent()
                }
            }
    }

/** A modifier's accessibility node, added only on request: radial effects are decoration and must not hide the content they sit on. */
private fun Modifier.radialSemantics(name: String, appendedA11y: String?, customA11y: String?): Modifier =
    if (appendedA11y == null && customA11y == null) this else cyberSemantics(name, appendedA11y, customA11y)

/**
 * Reports where the component sits in the window each time it is laid out, so a driver can answer
 * [CyberRadialField.intensityAt] queries from other components.
 *
 * @param region The region the effect is drawn with.
 * @param onPlaced Receives the placement after every layout pass.
 */
private fun Modifier.trackRadialPlacement(region: CyberRadialRegion, onPlaced: (CyberRadialPlacement) -> Unit): Modifier = composed {
    val density = LocalDensity.current
    onGloballyPositioned { onPlaced(CyberRadialPlacement(it.positionInRoot(), it.size.toSize(), region, density)) }
}

/**
 * Runs [block] clipped to [path], or unclipped when [path] is null.
 *
 * @param path Clip path in the current coordinate space, or null for no clip.
 * @param block Drawing to clip.
 */
private inline fun DrawScope.clipTo(path: Path?, block: DrawScope.() -> Unit) {
    if (path == null) block() else clipPath(path, block = block)
}

/**
 * Fills the ring between [innerPx] and [outerPx] around the origin of the current coordinate space with [brush].
 * With no inner radius it is a disc; otherwise a thick circle outline.
 *
 * @param brush Gradient to fill with, centered on the origin.
 * @param alpha Opacity multiplier applied on top of the brush's own alpha.
 * @param innerPx Inner radius in pixels; nothing is drawn inside it.
 * @param outerPx Outer radius in pixels.
 */
private fun DrawScope.drawRingFill(brush: Brush, alpha: Float, innerPx: Float, outerPx: Float) {
    if (innerPx <= 0f) {
        drawCircle(brush, radius = outerPx, center = Offset.Zero, alpha = alpha)
    } else {
        drawCircle(brush, radius = (innerPx + outerPx) / 2f, center = Offset.Zero, alpha = alpha, style = Stroke(width = outerPx - innerPx))
    }
}

/** Builds a pie-slice path around the origin for a sector, and keeps the last one until the sector or radius changes. */
private class PieCache {
    private var sector: CyberRadialSector? = null
    private var outer = Float.NaN
    private var path = Path()

    /**
     * The pie slice for [sector] reaching [outerPx], centered on the origin.
     *
     * @param sector Angular slice to cover.
     * @param outerPx Radius of the slice in pixels.
     */
    fun get(sector: CyberRadialSector, outerPx: Float): Path {
        if (sector != this.sector || outerPx != outer) {
            this.sector = sector
            outer = outerPx
            path = Path().apply {
                moveTo(0f, 0f)
                arcTo(Rect(Offset.Zero, outerPx), CyberRadialMath.toComposeDegrees(sector.startDegrees), sector.span, false)
                close()
            }
        }
        return path
    }
}

/** A wedge gradient ready to draw. [headFraction] is where the head sits on the gradient, 0..1. */
private class Wedge(val headFraction: Float, val brush: Brush)

/** Builds the sweep-wedge gradient and keeps the last one, which is reused for every frame of a constant-length trail. */
private class WedgeCache {
    private var trail = Float.NaN
    private var steps = -1
    private var clockwise = false
    private var color = Color.Unspecified
    private var alpha = Float.NaN
    private var wedge: Wedge? = null

    /**
     * The wedge for these settings.
     *
     * @param trailDegrees Trail length in degrees.
     * @param steps Opacity bands, or 0 for a smooth fade.
     * @param movingClockwise Whether the head moves clockwise.
     * @param color Wedge color.
     * @param alpha Opacity at the head.
     */
    fun get(trailDegrees: Float, steps: Int, movingClockwise: Boolean, color: Color, alpha: Float): Wedge {
        val cached = wedge
        if (cached != null && trailDegrees == trail && steps == this.steps && movingClockwise == clockwise && color == this.color && alpha == this.alpha) return cached
        trail = trailDegrees; this.steps = steps; clockwise = movingClockwise; this.color = color; this.alpha = alpha
        val stops = CyberRadialMath.sweepStops(trailDegrees, steps, movingClockwise)
        val brush = Brush.sweepGradient(
            *stops.stops.map { it.position to color.copy(alpha = it.alpha * alpha) }.toTypedArray(),
            center = Offset.Zero,
        )
        return Wedge(stops.headFraction, brush).also { wedge = it }
    }
}
