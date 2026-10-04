package com.example.cyberpunkandroid.effects

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberRadialDefaults
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import com.example.cyberpunkandroid.utils.CyberPathGeometry.hash01
import kotlin.math.floor

/**
 * **Particle shower.** Many small comets stream along a path at once: each has a hot rounded head and a tail that
 * fades behind it. They start at scattered positions, take different tail lengths, and run in a few speed classes;
 * the faster a comet, the brighter it is, which reads as depth. The scatter is deterministic from [seed], so the
 * same shower looks the same every time.
 *
 * ### Appearance
 * On a closed path (a border) the comets circle it. On an open path (a line from [cyberPathAlong] or
 * [cyberPathDivider]) they enter at the start and leave at the end. Each comet's tail is drawn as [steps]
 * overlapping translucent passes, so it fades from bright at the head to dim at the tail end in visible bands.
 *
 * ### Using it on a radial path
 * The path is whatever you give it, so a shower can run on a circle, an arc or a spiral:
 * - a circle or any shape: `Modifier.cyberPathBorder(CyberParticleShower(), shape = CircleShape)`;
 * - any path: `Modifier.cyberPathAlong(CyberParticleShower(), path = { size -> myPath(size) })`.
 *
 * All comets of one speed class share a few draw layers, so the cost depends on the number of speed classes and
 * [steps], not on [count].
 *
 * Every lap count in [speeds] is a whole number, so the loop is seamless.
 *
 * @param count Comets on the path at once.
 * @param tail Longest tail as a fraction of the path length, 0..1. Individual tails are between `MinTailScale` of this and all of it.
 * @param width Tail thickness.
 * @param head Length of the hot head of each comet.
 * @param speeds Whole laps per animation loop that comets can have. Each comet picks one; use one value for uniform speed.
 * @param steps Overlapping tail passes per comet; more gives a smoother fade.
 * @param seed Seed for the scatter of start positions, speeds and tail lengths. Change it for a different pattern.
 * @param trackAlpha Opacity of a dim line along the whole path under the comets. Zero draws none.
 */
data class CyberParticleShower(
    val count: Int = CyberRadialDefaults.Shower.Count,
    val tail: Float = CyberRadialDefaults.Shower.Tail,
    val width: Dp = CyberRadialDefaults.Shower.Width,
    val head: Dp = CyberRadialDefaults.Shower.Head,
    val speeds: List<Int> = CyberRadialDefaults.Shower.Speeds,
    val steps: Int = CyberRadialDefaults.Shower.Steps,
    val seed: Int = CyberRadialDefaults.Shower.Seed,
    val trackAlpha: Float = 0f,
) : CyberPathEffect {
    override fun extent(density: Density) = with(density) { width.toPx() * CyberRadialDefaults.Shower.HeadWidthScale / 2f }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val total = CyberPathGeometry.length(outline)
        val widthPx = width.toPx()
        val headPx = head.toPx()
        val longestTail = tail.coerceIn(0f, 1f) * total
        val travel = if (closed) total else total + longestTail // on an open path a comet leaves completely before it re-enters
        val classes = speeds.filter { it > 0 }.distinct().sorted().ifEmpty { listOf(1) }
        val fastest = classes.last()
        val slowest = classes.first()

        // One entry per comet, grouped by speed class so each class needs only a few draw layers.
        val phases = FloatArray(count)
        val tails = FloatArray(count)
        val classOf = IntArray(count)
        for (i in 0 until count) {
            val base = seed * CyberRadialDefaults.Shower.SeedStride + i * CyberRadialDefaults.Shower.IndexStride
            phases[i] = hash01(base + CyberRadialDefaults.Shower.PhaseSalt)
            classOf[i] = floor(hash01(base + CyberRadialDefaults.Shower.SpeedSalt) * classes.size).toInt().coerceIn(0, classes.size - 1)
            tails[i] = longestTail * (CyberRadialDefaults.Shower.MinTailScale + (1f - CyberRadialDefaults.Shower.MinTailScale) * hash01(base + CyberRadialDefaults.Shower.TailSalt))
        }

        val membersByClass = classes.indices.map { c -> (0 until count).filter { classOf[it] == c } }

        /** Turns flattened [from, to] spans into a dash layer, or null when nothing is visible. */
        fun layer(spans: FloatArray, width: Float, cap: StrokeCap, alpha: Float, hot: Float): CyberPathLayer? {
            val dash = if (closed) CyberPathGeometry.spansDash(total, spans) else CyberPathGeometry.openSpansDash(total, spans)
            return when {
                dash.full -> CyberPathLayer(null, width, cap, alpha, hot)
                dash.pathEffect != null -> CyberPathLayer(dash.pathEffect, width, cap, alpha, hot)
                else -> null
            }
        }

        CyberPathRenderer { p ->
            val out = ArrayList<CyberPathLayer>()
            if (trackAlpha > 0f) out.add(CyberPathLayer(null, widthPx * CyberRadialDefaults.Shower.TrackWidthScale, StrokeCap.Butt, trackAlpha, 0f, glow = false))
            for ((c, lapsPerLoop) in classes.withIndex()) {
                val members = membersByClass[c]
                if (members.isEmpty()) continue
                // Brightness grows with speed: the slowest class is SlowestAlpha, the fastest is 1.
                val classAlpha = if (fastest == slowest) 1f
                else CyberRadialDefaults.Shower.SlowestAlpha + (1f - CyberRadialDefaults.Shower.SlowestAlpha) * (lapsPerLoop - slowest) / (fastest - slowest)
                val heads = FloatArray(members.size) { k -> ((p * lapsPerLoop + phases[members[k]]).mod(1f)) * travel }
                for (pass in 1..steps) {
                    val spans = FloatArray(members.size * 2) { j ->
                        val k = j / 2
                        if (j % 2 == 0) heads[k] - tails[members[k]] * pass / steps else heads[k]
                    }
                    layer(spans, widthPx, StrokeCap.Butt, CyberRadialDefaults.Shower.TailPassAlpha * classAlpha, 0f)?.let { out.add(it) }
                }
                val headSpans = FloatArray(members.size * 2) { j -> if (j % 2 == 0) heads[j / 2] - headPx else heads[j / 2] }
                layer(headSpans, widthPx * CyberRadialDefaults.Shower.HeadWidthScale, StrokeCap.Round, classAlpha, CyberRadialDefaults.Shower.HeadHot)?.let { out.add(it) }
            }
            out
        }
    }
}
