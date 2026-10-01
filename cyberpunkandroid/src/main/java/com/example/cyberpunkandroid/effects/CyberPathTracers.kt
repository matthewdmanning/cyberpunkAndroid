package com.example.cyberpunkandroid.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import com.example.cyberpunkandroid.utils.CyberPathGeometry.fit
import com.example.cyberpunkandroid.utils.CyberPathGeometry.hash01
import com.example.cyberpunkandroid.utils.CyberPathGeometry.smoothstep
import com.example.cyberpunkandroid.utils.CyberPathGeometry.window
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

private fun android.graphics.PathEffect.asComposePathEffect(): PathEffect {
    return try {
        val constructor = Class.forName("androidx.compose.ui.graphics.AndroidPathEffect")
            .getDeclaredConstructor(android.graphics.PathEffect::class.java)
        constructor.isAccessible = true
        constructor.newInstance(this) as PathEffect
    } catch (_: Throwable) {
        object : PathEffect {}
    }
}

// Tracers: light that moves along the outline. Each maps one animation cycle (progress 0..1,
// driven by the modifier's hoisted animationSpec) to a set of glowing stroke layers.
// "hot" layers are mixed toward white: the overexposed core of a neon head.

/** Dim full-outline pass so the path of the light stays readable. */
private fun track(width: Float, alpha: Float) = CyberPathLayer(null, width, StrokeCap.Butt, alpha, 0f, glow = false)

/**
 * **Comet tracer.** A white-hot head with a cyan tail that fades behind it, running the outline.
 * [count] comets share the loop evenly. With [stutter] > 0 the comet jumps in that many discrete
 * steps per loop and leaves [ghosts] dimmer afterimages at its previous positions (a digital,
 * frame-skipping look). On open lines the comet enters at the start and fully exits at the end.
 *
 * @param tail Tail length as a fraction of the outline length.
 * @param steps Number of overlapping tail passes; more is a smoother fade.
 * @param trackAlpha Opacity of the dim track under the comet (0 = none).
 */
data class CyberCometTracer(
    val count: Int = 1,
    val tail: Float = 0.30f,
    val width: Dp = CyberPathDefaults.Comet.Width,
    val head: Dp = CyberPathDefaults.Comet.Head,
    val steps: Int = 10,
    val trackAlpha: Float = 0.14f,
    val stutter: Int = 0,
    val ghosts: Int = 3,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * 1.15f / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val period = if (closed) length / count else length
        val tailPx = min(tail * length, period * 0.9f) // tail never laps the next comet
        val span = if (closed) length else length + tailPx
        val w = width.toPx()
        val headPx = head.toPx()

        fun comet(s: Float, alphaScale: Float, out: MutableList<CyberPathLayer>) {
            for (k in 1..steps) {
                val len = tailPx * k / steps
                // 0.20 per pass: overlapping passes build a bright-to-dim ramp toward the tail end
                window(length, s - len, len, period, closed)?.let { out.add(CyberPathLayer(it, w, StrokeCap.Butt, 0.20f * alphaScale)) }
            }
            window(length, s - headPx, headPx, period, closed)?.let {
                out.add(CyberPathLayer(it, w * 1.15f, StrokeCap.Round, alphaScale, hot = 0.75f))
            }
        }

        CyberPathRenderer { p ->
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(track(w * 0.75f, trackAlpha))
            if (stutter > 0) {
                val q = floor(p * stutter) / stutter
                for (g in ghosts downTo 1) {
                    // afterimages fade with age; 0.35 keeps the newest ghost well below the live comet
                    comet(((q - g.toFloat() / stutter).mod(1f)) * span, 0.35f * (1f - g.toFloat() / (ghosts + 1)), out)
                }
                comet(q * span, 1f, out)
            } else {
                comet(p * span, 1f, out)
            }
            out
        }
    }
}

/**
 * **Corner charge.** Light grows out of every corner in both directions until the segments meet
 * and the whole outline is lit, then the outline flashes white-hot and (with [fadeOut]) fades so
 * the cycle can repeat. Growing ends carry small hot heads. Circles charge from four points.
 *
 * Choreography within one cycle: grow 0–0.72, flash peak 0.72–0.80, fade 0.92–1.0.
 */
data class CyberCornerCharge(
    val width: Dp = CyberPathDefaults.Charge.Width,
    val head: Dp = CyberPathDefaults.Charge.Head,
    val trackAlpha: Float = 0.12f,
    val fadeOut: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * 1.3f / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val centers: FloatArray
        val next: FloatArray
        val prev: FloatArray
        if (closed) {
            centers = CyberPathGeometry.cornerCenters(outline, length, CyberPathDefaults.Geometry.CornerSample.toPx(), CyberPathDefaults.Geometry.CornerMaxRadius.toPx(), CyberPathDefaults.Geometry.CornerGapTolerance.toPx())
                .takeIf { it.isNotEmpty() } ?: FloatArray(4) { k -> length * (k / 4f + 1f / 8f) }
            val n = centers.size
            next = FloatArray(n) { i -> (centers[(i + 1) % n] - centers[i]).mod(length).let { if (it == 0f) length else it } }
            prev = FloatArray(n) { i -> next[(i - 1).mod(n)] }
        } else {
            centers = floatArrayOf(0f, length) // open lines charge inward from both ends
            next = floatArrayOf(length, 0f)
            prev = floatArrayOf(0f, length)
        }
        val w = width.toPx()
        val headPx = head.toPx()

        CyberPathRenderer { p ->
            val grow = smoothstep(0f, 0.72f, p)
            val flash = smoothstep(0.72f, 0.78f, p) * (1f - smoothstep(0.80f, 1f, p))
            val fade = if (fadeOut) 1f - smoothstep(0.92f, 1f, p) else 1f
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(track(w * 0.75f, trackAlpha))
            val spans = FloatArray(centers.size * 2)
            val heads = FloatArray(centers.size * 4)
            for (i in centers.indices) {
                var l0 = centers[i] - grow * prev[i] / 2f
                var l1 = centers[i] + grow * next[i] / 2f
                if (!closed) { l0 = max(0f, l0); l1 = min(length, l1) }
                spans[2 * i] = l0; spans[2 * i + 1] = l1
                heads[4 * i] = l0; heads[4 * i + 1] = l0 + headPx
                heads[4 * i + 2] = l1 - headPx; heads[4 * i + 3] = l1
            }
            val lit = if (closed) CyberPathGeometry.spansDash(length, spans) else CyberPathGeometry.openSpansDash(length, spans)
            val hotHeads = if (grow < 0.999f) {
                (if (closed) CyberPathGeometry.spansDash(length, heads) else CyberPathGeometry.openSpansDash(length, heads)).pathEffect
            } else null
            if (lit.full) out.add(CyberPathLayer(null, w, StrokeCap.Butt, 0.85f * fade))
            else if (lit.pathEffect != null) out.add(CyberPathLayer(lit.pathEffect, w, StrokeCap.Butt, 0.85f * fade))
            if (hotHeads != null) out.add(CyberPathLayer(hotHeads, w * 1.2f, StrokeCap.Butt, fade, hot = 0.8f))
            if (flash > 0.01f) out.add(CyberPathLayer(null, w * 1.3f, StrokeCap.Butt, flash, hot = 0.75f))
            out
        }
    }
}

/**
 * **Draw-on.** The outline traces itself: on closed shapes two hot heads leave the start point
 * (the middle of the longest side) in opposite directions and meet on the far side; open lines
 * draw from the start. Then it holds and (with [fadeOut]) fades.
 *
 * Choreography within one cycle: draw 0–0.6, hold, fade 0.85–1.0.
 */
data class CyberDrawOn(
    val width: Dp = CyberPathDefaults.DrawOn.Width,
    val head: Dp = CyberPathDefaults.DrawOn.Head,
    val fadeOut: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * 1.2f / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val length = CyberPathGeometry.length(outline)
        val w = width.toPx()
        val headPx = head.toPx()
        CyberPathRenderer { p ->
            val g = smoothstep(0f, 0.6f, p)
            val fade = if (fadeOut) 1f - smoothstep(0.85f, 1f, p) else 1f
            val out = ArrayList<CyberPathLayer>()
            if (!closed) {
                val len = g * length
                if (len >= length - 0.5f) return@CyberPathRenderer listOf(CyberPathLayer(null, w, StrokeCap.Butt, fade))
                if (len > 0.2f) {
                    window(length, 0f, len, length, false)?.let { out.add(CyberPathLayer(it, w, StrokeCap.Butt, fade)) }
                    window(length, len - headPx, headPx, length, false)?.let { out.add(CyberPathLayer(it, w * 1.2f, StrokeCap.Round, fade, hot = 0.85f)) }
                }
                return@CyberPathRenderer out
            }
            val half = g * length / 2f
            if (half >= length / 2f - 0.5f) {
                out.add(CyberPathLayer(null, w, StrokeCap.Butt, fade))
            } else if (half > 0.2f) {
                window(length, -half, 2f * half)?.let { out.add(CyberPathLayer(it, w, StrokeCap.Butt, fade)) }
                CyberPathGeometry.spansDash(length, floatArrayOf(-half, -half + headPx, half - headPx, half)).pathEffect?.let {
                    out.add(CyberPathLayer(it, w * 1.2f, StrokeCap.Round, fade, hot = 0.85f))
                }
            }
            out
        }
    }
}

/**
 * **Scanner.** A KITT-style light that sweeps to one end and back, slowing at each end, with a
 * trail that flips to the opposite side when it turns. Designed for dividers; on closed shapes it
 * sweeps the whole perimeter back and forth.
 *
 * @param length Trail length as a fraction of the path length.
 */
data class CyberScanner(
    val width: Dp = CyberPathDefaults.Scanner.Width,
    val length: Float = 0.18f,
    val steps: Int = 8,
    val trackAlpha: Float = 0.12f,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * 1.2f / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val trail = length * total
        val w = width.toPx()
        val headPx = CyberPathDefaults.Scanner.Head.toPx()
        CyberPathRenderer { p ->
            val u = 0.5f - 0.5f * cos(2.0 * PI * p).toFloat() // 0 -> 1 -> 0 with slow ends
            val forward = p < 0.5f
            // keep the head 15% of a trail length off each end so the trail stays on the path
            val s = u * (total - trail * 0.3f) + trail * 0.15f
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(track(w * 0.6f, trackAlpha))
            for (k in 1..steps) {
                val len = trail * k / steps
                window(total, if (forward) s - len else s, len, total, closed)?.let { out.add(CyberPathLayer(it, w, StrokeCap.Butt, 0.22f)) }
            }
            window(total, s - headPx / 2f, headPx, total, closed)?.let { out.add(CyberPathLayer(it, w * 1.2f, StrokeCap.Round, 1f, hot = 0.8f)) }
            out
        }
    }
}

/**
 * **Live wire.** A jittering electric arc crawls along the outline: two jagged passes (a wide
 * one and a thin white-hot one) that re-roll their shape [frames] times per loop and flicker in
 * brightness. Uses Android's `DiscretePathEffect` for the jitter.
 *
 * @param length Arc length as a fraction of the outline length.
 */
data class CyberLiveWire(
    val width: Dp = CyberPathDefaults.LiveWire.Width,
    val length: Float = 0.22f,
    val jitter: Dp = CyberPathDefaults.LiveWire.Jitter,
    val segment: Dp = CyberPathDefaults.LiveWire.Segment,
    val frames: Int = 60,
    val count: Int = 1,
    val trackAlpha: Float = 0.10f,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { jitter.toPx() + width.toPx() }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val period = if (closed) total / count else total
        val arc = min(length * total, period * 0.8f)
        val w = width.toPx()
        val dev = jitter.toPx()
        val seg = segment.toPx()
        CyberPathRenderer { p ->
            val f = floor(p * frames).toDouble()
            val j1 = 1f + 0.35f * hash01(f + 1)  // segment length wobble re-rolls the jagged shape
            val j2 = 1f + 0.35f * hash01(f + 91)
            val flicker = 0.65f + 0.35f * hash01(f + 7)
            val s = p * (if (closed) total else total + arc)
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(track(w * 0.7f, trackAlpha))
            val win = window(total, s - arc, arc, period, closed) ?: return@CyberPathRenderer out
            val wide = PathEffect.chainPathEffect(android.graphics.DiscretePathEffect(seg * j1, dev).asComposePathEffect(), win)
            val core = PathEffect.chainPathEffect(android.graphics.DiscretePathEffect(seg * 0.6f * j2, dev * 0.7f).asComposePathEffect(), win)
            out.add(CyberPathLayer(wide, w, StrokeCap.Round, flicker, hot = 0.35f))
            out.add(CyberPathLayer(core, w * 0.6f, StrokeCap.Round, flicker * 0.9f, hot = 0.9f))
            out
        }
    }
}

/** One lane of [CyberPacketStream]: [count] packets of [length], moving [speed]x per loop (integer = seamless). */
data class CyberPacketLane(val count: Int, val length: Dp, val speed: Int, val alpha: Float)

/**
 * **Packet stream.** Several lanes of data packets on the same outline, each lane with its own
 * packet size, count and speed, so fast small packets overtake slow long ones. Each packet has a
 * hot leading edge. Integer speeds keep the loop seamless.
 */
data class CyberPacketStream(
    val width: Dp = CyberPathDefaults.Packets.Width,
    val trackAlpha: Float = 0.12f,
    val lanes: List<CyberPacketLane> = listOf(
        CyberPacketLane(1, CyberPathDefaults.Packets.LongPacket, 1, 1.0f),
        CyberPacketLane(2, CyberPathDefaults.Packets.MediumPacket, 2, 0.85f),
        CyberPacketLane(3, CyberPathDefaults.Packets.ShortPacket, 3, 0.7f),
    ),
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * 1.1f / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val w = width.toPx()
        val edge = CyberPathDefaults.Packets.Edge.toPx() // hot leading edge
        val lanePx = lanes.map { it to it.length.toPx() }
        CyberPathRenderer { p ->
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(track(w * 0.7f, trackAlpha))
            for ((lane, len) in lanePx) {
                val u = (p * lane.speed).mod(1f)
                for (k in 0 until lane.count) {
                    val s0 = if (closed) u * total + k * total / lane.count else ((u + k.toFloat() / lane.count).mod(1f)) * (total + len)
                    window(total, s0 - len, len, total, closed)?.let { out.add(CyberPathLayer(it, w, StrokeCap.Butt, lane.alpha, hot = 0.3f)) }
                    window(total, s0 - edge, edge, total, closed)?.let { out.add(CyberPathLayer(it, w * 1.1f, StrokeCap.Butt, lane.alpha, hot = 0.9f)) }
                }
            }
            out
        }
    }
}

/**
 * **Charge meter.** The outline is a ring of segments; they light up one after another, the
 * leading segment flickering as it powers on, until the ring is full; then (with [fadeOut]) it
 * fades for the next cycle. Unlit segments stay faintly visible.
 *
 * Choreography within one cycle: fill 0–0.8, hold, fade 0.92–1.0.
 */
data class CyberChargeMeter(
    val segment: Dp = CyberPathDefaults.Meter.Segment,
    val gap: Dp = CyberPathDefaults.Meter.Gap,
    val height: Dp = CyberPathDefaults.Meter.Height,
    val trackAlpha: Float = 0.16f,
    val frames: Int = 60,
    val fadeOut: Boolean = true,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { height.toPx() / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val want = segment.toPx() + gap.toPx()
        val per = fit(total, want)
        val n = (total / per).roundToInt()
        val g = gap.toPx() * per / want
        val on = per - g
        val h = height.toPx()
        val dim = PathEffect.dashPathEffect(floatArrayOf(on, g), 0f)
        CyberPathRenderer { p ->
            val fill = smoothstep(0f, 0.8f, p)
            val k = (fill * n + 1e-6f).toInt()
            val fade = if (fadeOut) 1f - smoothstep(0.92f, 1f, p) else 1f
            val out = ArrayList<CyberPathLayer>()
            out.add(CyberPathLayer(dim, h, StrokeCap.Butt, trackAlpha, glow = false))
            if (k > 0) {
                val iv = FloatArray(2 * k) { i -> if (i % 2 == 0) on else g }
                iv[iv.size - 1] = total - k * per + g // one long gap covers the unlit remainder
                out.add(CyberPathLayer(PathEffect.dashPathEffect(iv, 0f), h, StrokeCap.Butt, 0.9f * fade, hot = 0.15f))
            }
            if (k in 1 until n) {
                val a = 0.35f + 0.65f * hash01(floor(p * frames).toDouble() + 3)
                window(total, k * per, on, total, closed)?.let { out.add(CyberPathLayer(it, h, StrokeCap.Butt, a * fade, hot = 0.6f)) }
            }
            out
        }
    }
}

/**
 * **Sequenced lights.** A border of dim chevrons pointing along the path; one chevron per
 * [groups] lights up at a time and steps forward light by light, leaving a decaying afterglow of
 * [trail] lights behind it, like runway approach strobes.
 */
data class CyberSequencedLights(
    val groups: Int = 2,
    val trail: Int = 5,
    val size: Dp = CyberPathDefaults.Lights.Size,
    val spacing: Dp = CyberPathDefaults.Lights.Spacing,
    val width: Dp = CyberPathDefaults.Lights.Width,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { size.toPx() + width.toPx() }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val sp = fit(total, spacing.toPx())
        val n = (total / sp).roundToInt()
        val a = size.toPx()
        val stamp = CyberPathGeometry.ribbon(
            CyberPathGeometry.subdivide(listOf(Offset(-a, -a), Offset.Zero, Offset(-a, a)), CyberPathDefaults.Geometry.FineStep.toPx()), width.toPx(),
        )
        val dim = CyberPathGeometry.stamp(stamp, sp, 0f, StampedPathEffectStyle.Rotate)
        val perGroup = max(1, n / groups)
        val period = perGroup * sp // one lit chevron per group: a stamp every period, positioned by phase
        CyberPathRenderer { p ->
            val j = floor(p * perGroup).toInt()
            val out = ArrayList<CyberPathLayer>()
            out.add(CyberPathLayer(dim, 1f, StrokeCap.Butt, 0.22f, glow = false))
            for (q in 0 until trail) {
                val pos = ((j - q).mod(perGroup)) * sp
                val lit = CyberPathGeometry.stamp(stamp, period, (-pos).mod(period), StampedPathEffectStyle.Rotate)
                val alpha = (1f - q.toFloat() / trail).pow(1.6f) // afterglow decay
                out.add(CyberPathLayer(lit, 1f, StrokeCap.Butt, alpha, hot = if (q == 0) 0.7f else 0.1f))
            }
            out
        }
    }
}

/**
 * **Bracket lock.** Corner brackets (see [CyberCornerBrackets]) snap in from nothing with a
 * slight overshoot, flash white-hot as they lock, then breathe gently.
 *
 * Choreography within one cycle: snap 0–0.35 (ease-out-back), flash 0.33–0.55, breathe after 0.45.
 */
data class CyberBracketLock(
    val arm: Dp = CyberPathDefaults.Brackets.Arm,
    val width: Dp = CyberPathDefaults.Brackets.Width,
    val notches: Boolean = true,
) : CyberPathEffect {
    private val brackets get() = CyberCornerBrackets(arm, width, notches)

    override fun extent(density: Density) = with(density) { width.toPx() / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val base = brackets
        val w = width.toPx()
        CyberPathRenderer { p ->
            val t = min(1f, p / 0.35f)
            val c1 = 1.70158f // standard ease-out-back overshoot constants
            val c3 = c1 + 1f
            val back = 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
            val flash = smoothstep(0.33f, 0.38f, p) * (1f - smoothstep(0.40f, 0.55f, p))
            val breathe = 0.75f + 0.25f * cos(2.0 * PI * max(0f, p - 0.45f) / 0.55f * 2f).toFloat()
            val pe = base.effectFor(outline, closed, total, density, max(0.02f, back))
            val out = ArrayList<CyberPathLayer>()
            out.add(CyberPathLayer(pe, w, StrokeCap.Butt, if (p > 0.45f) breathe else 1f, hot = 0.25f + 0.6f * flash))
            if (flash > 0.01f) out.add(CyberPathLayer(null, w * 0.6f, StrokeCap.Butt, 0.5f * flash, hot = 0.9f))
            out
        }
    }
}
