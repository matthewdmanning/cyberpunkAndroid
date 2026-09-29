package com.example.cyberpunkandroid.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.utils.CyberMarching
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import com.example.cyberpunkandroid.utils.CyberPathGeometry.fit
import com.example.cyberpunkandroid.utils.CyberPathGeometry.merge
import com.example.cyberpunkandroid.utils.CyberPathGeometry.polygon
import com.example.cyberpunkandroid.utils.CyberPathGeometry.ribbon
import com.example.cyberpunkandroid.utils.CyberPathGeometry.subdivide
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin

// Box patterns: geometric border styles. All accept progress, so with an animating
// cyberPathBorder they march along the outline; with trigger = NONE they are static.
// Every spacing is refitted to the outline length so closed shapes have no seam.

private fun cornerCentersFor(path: Path, length: Float, density: Density): FloatArray = with(density) {
    CyberPathGeometry.cornerCenters(path, length, CyberPathDefaults.Geometry.CornerSample.toPx(), CyberPathDefaults.Geometry.CornerMaxRadius.toPx(), CyberPathDefaults.Geometry.CornerGapTolerance.toPx())
}

/** Evenly spaced stand-ins (at 45/135/225/315 degrees on a circle) for outlines with no corners. */
private fun quadrants(length: Float) = FloatArray(4) { k -> length * (k / 4f + 1f / 8f) }

/**
 * **Corner brackets.** Strokes only the corners of any outline: sharp corners get L-shaped
 * brackets, chamfers get a bracket that wraps the whole chamfer, rounded corners get arcs.
 * Circles get four quadrant arcs. With [notches], a short tick marks the middle of each long
 * side, like a targeting frame. Open lines get end caps and a centre notch.
 * Static: ignores progress (see [CyberBracketLock] for the animated version).
 */
data class CyberCornerBrackets(
    val arm: Dp = CyberPathDefaults.Brackets.Arm,
    val width: Dp = CyberPathDefaults.Brackets.Width,
    val notches: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() / 2f }

    internal fun effectFor(outline: Path, closed: Boolean, length: Float, density: Density, armScale: Float): PathEffect? {
        val armPx = with(density) { arm.toPx() }
        if (!closed) {
            val a = min(armPx * armScale, length / 3f)
            val spans = ArrayList<Float>().apply {
                addAll(listOf(0f, a, length - a, length))
                if (notches) addAll(listOf(length / 2f - a * 0.35f, length / 2f + a * 0.35f))
            }
            return CyberPathGeometry.openSpansDash(length, spans.toFloatArray()).pathEffect
        }
        val centers = cornerCentersFor(outline, length, density).takeIf { it.isNotEmpty() } ?: quadrants(length)
        val n = centers.size
        val gaps = FloatArray(n) { i ->
            val g = (centers[(i + 1) % n] - centers[i]).mod(length)
            if (g == 0f) length else g
        }
        val a = min(armPx, 0.3f * gaps.min()) * armScale // 0.3: brackets never meet on short sides
        val spans = ArrayList<Float>()
        for (c in centers) { spans.add(c - a); spans.add(c + a) }
        if (notches) {
            for (i in 0 until n) {
                // notch only on sides longer than ~3 bracket arms (unscaled so notches don't pop during lock-in)
                if (gaps[i] > 6f * a / max(armScale, 1e-3f)) {
                    val m = centers[i] + gaps[i] / 2f
                    spans.add(m - a * 0.35f); spans.add(m + a * 0.35f)
                }
            }
        }
        return CyberPathGeometry.spansDash(length, spans.toFloatArray()).pathEffect
    }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer {
        val length = CyberPathGeometry.length(outline)
        val layer = listOf(CyberPathLayer(effectFor(outline, closed, length, density, 1f), with(density) { width.toPx() }))
        return CyberPathRenderer { layer }
    }
}

/**
 * **Graduated ticks.** An instrument scale: thin minor ticks pointing into the shape, a longer
 * major tick every [every]th, and an optional spine line along the outline. On a circle it reads
 * as a dial bezel. Ticks are placed rigidly, so none straddle a corner. Progress rotates the scale.
 */
data class CyberGraduatedTicks(
    val spacing: Dp = CyberPathDefaults.Ticks.Spacing,
    val minor: Dp = CyberPathDefaults.Ticks.Minor,
    val major: Dp = CyberPathDefaults.Ticks.Major,
    val every: Int = 5,
    val width: Dp = CyberPathDefaults.Ticks.Width,
    val spine: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val big = fit(length, spacing.toPx() * every)
        val sp = big / every
        val w = width.toPx()
        fun tick(len: Float) = polygon(listOf(Offset(-w / 2, 0f), Offset(w / 2, 0f), Offset(w / 2, len), Offset(-w / 2, len)))
        val shapes = buildList {
            add(Triple(tick(minor.toPx()), StampedPathEffectStyle.Rotate, sp))
            add(Triple(tick(major.toPx()), StampedPathEffectStyle.Rotate, big))
            if (spine) add(Triple(ribbon(subdivide(listOf(Offset.Zero, Offset(big, 0f)), CyberPathDefaults.Geometry.MorphStep.toPx()), w), StampedPathEffectStyle.Morph, big))
        }
        val marching = CyberMarching(outline, closed, big)
        CyberPathRenderer { p ->
            shapes.map { (shape, style, adv) ->
                val (pe, path) = marching.at(shape, style, p, adv)
                CyberPathLayer(pe, 1f, path = path)
            }
        }
    }
}

/**
 * **Barcode.** A thick band of bars and spaces of varying width, like a machine-readable label
 * wrapped round the outline. The pattern comes from [seed], so it is stable between runs.
 * Progress scrolls the bars.
 */
data class CyberBarcode(
    val module: Dp = CyberPathDefaults.Barcode.Module,
    val height: Dp = CyberPathDefaults.Barcode.Height,
    val seed: Int = 7,
    val bars: Int = 18,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { height.toPx() / 2f }

    /** Bar/space widths in modules, from a portable LCG so the pattern is identical everywhere. */
    internal fun units(): IntArray {
        var state = seed.toLong() and 0x7FFFFFFFL
        fun next(k: Int): Int {
            state = (state * 1103515245L + 12345L) and 0x7FFFFFFFL
            return (state % k).toInt()
        }
        val barWidths = intArrayOf(1, 1, 2, 3, 4)
        val spaceWidths = intArrayOf(1, 1, 1, 2, 3)
        return IntArray(bars * 2) { i -> if (i % 2 == 0) barWidths[next(5)] else spaceWidths[next(5)] }
    }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val u = units()
        val total = u.sum()
        val length = CyberPathGeometry.length(outline)
        val k = fit(length, total * module.toPx()) / total
        val intervals = FloatArray(u.size) { u[it] * k }
        val period = intervals.sum()
        val h = height.toPx()
        CyberPathRenderer { p ->
            listOf(CyberPathLayer(PathEffect.dashPathEffect(intervals, (-p * period).mod(period)), h, StrokeCap.Butt))
        }
    }
}

/**
 * **Hazard band.** A band of slanted stripes that bends around corners, like hazard tape
 * following the border. Progress marches the stripes along the outline.
 */
data class CyberHazardBand(
    val band: Dp = CyberPathDefaults.Hazard.Band,
    val stripe: Dp = CyberPathDefaults.Hazard.Stripe,
    val slant: Dp = CyberPathDefaults.Hazard.Slant,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { band.toPx() / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val sp = fit(length, stripe.toPx() * 2f)
        val w = band.toPx()
        val k = slant.toPx()
        val shape = polygon(
            listOf(Offset(0f, -w / 2), Offset(sp * 0.5f, -w / 2), Offset(sp * 0.5f + k, w / 2), Offset(k, w / 2)),
            step = CyberPathDefaults.Geometry.MorphStep.toPx(),
        )
        val marching = CyberMarching(outline, closed, sp)
        CyberPathRenderer { p ->
            val (pe, path) = marching.at(shape, StampedPathEffectStyle.Morph, p)
            listOf(CyberPathLayer(pe, 1f, path = path))
        }
    }
}

/**
 * **Braid.** Hairline sine strands phase-shifted against each other so they weave along the
 * outline; with [rails] the weave runs between two parallel hairlines, like a guilloché band on
 * a banknote. Progress makes the weave flow.
 */
data class CyberBraid(
    val wavelength: Dp = CyberPathDefaults.Braid.Wavelength,
    val amplitude: Dp = CyberPathDefaults.Braid.Amplitude,
    val strands: Int = 3,
    val hairline: Dp = CyberPathDefaults.Braid.Hairline,
    val rails: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) {
        amplitude.toPx() + if (rails) CyberPathDefaults.Braid.RailGap.toPx() + hairline.toPx() else hairline.toPx()
    }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val wl = fit(length, wavelength.toPx())
        val amp = amplitude.toPx()
        val th = hairline.toPx()
        val parts = ArrayList<Path>()
        for (k in 0 until strands) {
            // 48 samples per wavelength keep the sine smooth after morphing
            val pts = List(49) { i -> Offset(wl * i / 48f, amp * sin(2.0 * PI * (i / 48.0 + k.toDouble() / strands)).toFloat()) }
            parts.add(ribbon(pts, th))
        }
        if (rails) {
            val r = amp + CyberPathDefaults.Braid.RailGap.toPx() // rails sit just outside the weave
            parts.add(ribbon(subdivide(listOf(Offset(0f, -r), Offset(wl, -r)), CyberPathDefaults.Geometry.MorphStep.toPx()), th))
            parts.add(ribbon(subdivide(listOf(Offset(0f, r), Offset(wl, r)), CyberPathDefaults.Geometry.MorphStep.toPx()), th))
        }
        val shape = merge(*parts.toTypedArray())
        val marching = CyberMarching(outline, closed, wl)
        CyberPathRenderer { p ->
            val (pe, path) = marching.at(shape, StampedPathEffectStyle.Morph, p)
            listOf(CyberPathLayer(pe, 1f, path = path))
        }
    }
}

/**
 * **Barbed wire.** Two twisted strands with a crossed barb every [twistsPerBarb] twists.
 * Progress drags the wire along the outline.
 */
data class CyberBarbedWire(
    val twist: Dp = CyberPathDefaults.BarbedWire.Twist,
    val twistsPerBarb: Int = 3,
    val barb: Dp = CyberPathDefaults.BarbedWire.Barb,
    val wire: Dp = CyberPathDefaults.BarbedWire.Wire,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { barb.toPx() }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val adv = fit(length, twist.toPx() * twistsPerBarb)
        val amp = CyberPathDefaults.BarbedWire.StrandOffset.toPx()
        val th = wire.toPx()
        val parts = ArrayList<Path>()
        for (k in 0 until 2) {
            val pts = List(73) { i -> Offset(adv * i / 72f, amp * sin(2.0 * PI * twistsPerBarb * i / 72.0 + k * PI).toFloat()) }
            parts.add(ribbon(pts, th))
        }
        val b = barb.toPx()
        val cx = adv * 0.5f
        parts.add(ribbon(listOf(Offset(cx - b * 0.55f, -b), Offset(cx + b * 0.55f, b)), th))
        parts.add(ribbon(listOf(Offset(cx + b * 0.55f, -b), Offset(cx - b * 0.55f, b)), th))
        val shape = merge(*parts.toTypedArray())
        val marching = CyberMarching(outline, closed, adv)
        CyberPathRenderer { p ->
            val (pe, path) = marching.at(shape, StampedPathEffectStyle.Morph, p)
            listOf(CyberPathLayer(pe, 1f, path = path))
        }
    }
}

/**
 * **Circuit trace.** A PCB trace along the outline with a hollow via ring every [spacing] and
 * a 45-degree branch stub ending in a solder pad pointing into the shape. Progress routes the
 * trace along the outline.
 */
data class CyberCircuitTrace(
    val spacing: Dp = CyberPathDefaults.Circuit.Spacing,
    val width: Dp = CyberPathDefaults.Circuit.Width,
    val via: Dp = CyberPathDefaults.Circuit.Via,
    val stub: Dp = CyberPathDefaults.Circuit.Stub,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { via.toPx() + width.toPx() }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val adv = fit(length, spacing.toPx())
        val th = width.toPx()
        val r = via.toPx()
        val vx = adv * 0.72f // via position within one repeat
        val step = CyberPathDefaults.Geometry.MorphStep.toPx()
        val line = merge(
            ribbon(subdivide(listOf(Offset.Zero, Offset(vx - r, 0f)), step), th),
            ribbon(subdivide(listOf(Offset(vx + r, 0f), Offset(adv, 0f)), step), th),
        )
        val viaRing = CyberPathGeometry.ring(vx, 0f, r, th, segments = 16)
        val bx = adv * 0.2f // branch position within one repeat
        val d = stub.toPx()
        val branch = ribbon(subdivide(listOf(Offset(bx, 0f), Offset(bx + d * 0.7f, d * 0.7f), Offset(bx + d * 1.6f, d * 0.7f)), CyberPathDefaults.Geometry.StubStep.toPx()), th)
        val pad = CyberPathGeometry.circle(bx + d * 1.6f, d * 0.7f, CyberPathDefaults.Circuit.Pad.toPx(), hole = false, segments = 12)
        val shape = merge(line, viaRing, branch, pad)
        val marching = CyberMarching(outline, closed, adv)
        CyberPathRenderer { p ->
            val (pe, path) = marching.at(shape, StampedPathEffectStyle.Morph, p)
            listOf(CyberPathLayer(pe, 1f, path = path))
        }
    }
}

/**
 * **Chain.** Face-on capsule links alternating with edge-on links, bent around corners like a
 * real chain. Progress pulls the chain along the outline.
 */
data class CyberChain(
    val link: Dp = CyberPathDefaults.Chain.Link,
    val thickness: Dp = CyberPathDefaults.Chain.Thickness,
    val wire: Dp = CyberPathDefaults.Chain.Wire,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { thickness.toPx() / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val adv = fit(length, link.toPx() * 1.75f) // one face link + one edge link
        val ow = link.toPx()
        val oh = thickness.toPx()
        val th = wire.toPx()
        val step = CyberPathDefaults.Geometry.MorphStep.toPx()
        val face = merge(
            CyberPathGeometry.stadium(0f, -oh / 2, ow, oh / 2, step),
            CyberPathGeometry.stadium(th, -oh / 2 + th, ow - th, oh / 2 - th, step, hole = true),
        )
        val edge = CyberPathGeometry.stadium(ow - th * 1.4f, -th * 0.7f, adv + th * 1.4f, th * 0.7f, step)
        val shape = merge(face, edge)
        val marching = CyberMarching(outline, closed, adv)
        CyberPathRenderer { p ->
            val (pe, path) = marching.at(shape, StampedPathEffectStyle.Morph, p)
            listOf(CyberPathLayer(pe, 1f, path = path))
        }
    }
}
