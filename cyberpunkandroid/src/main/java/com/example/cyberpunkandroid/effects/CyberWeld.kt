package com.example.cyberpunkandroid.effects

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.config.CyberPathDefaults
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.utils.CyberPathGeometry
import com.example.cyberpunkandroid.utils.CyberPathGeometry.hash01
import com.example.cyberpunkandroid.utils.CyberPathGeometry.window
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Solids stop visibly glowing below this temperature (the Draper point, 798 K). */
private const val DRAPER_KELVIN = 798f

/** Room temperature the bead cools toward. */
private const val AMBIENT_KELVIN = 300f

/** Deterministic per-particle random value; salts keep each property independent. */
private fun rand(index: Int, salt: Int): Float = hash01(index * 3.1 + salt * 17.7)

/** Color at [kelvin] from (kelvin, color) stops sorted by kelvin, clamped at both ends. */
internal fun colorAtKelvin(stops: List<Pair<Float, Color>>, kelvin: Float): Color {
    if (kelvin <= stops.first().first) return stops.first().second
    for (i in 0 until stops.size - 1) {
        val (k0, c0) = stops[i]
        val (k1, c1) = stops[i + 1]
        if (kelvin <= k1) return lerp(c0, c1, (kelvin - k0) / (k1 - k0))
    }
    return stops.last().second
}

/**
 * **Weld.** A welding arc runs the outline: a white-hot core with a blue halo that flickers in
 * brightness every frame, with occasional flares. A stream of tiny fizzing sparks sprays off the
 * arc, and every so often a pop throws a few larger, faster sparks, some of which burst into a
 * small star as they die. Sparks fly outward and fall under screen-down gravity, cooling from
 * white-yellow through orange to red.
 *
 * Behind the arc it leaves a jittered weld bead that cools slowly: yellow-white at the molten pool,
 * then orange, cherry red and dull red, until it stops glowing and settles to [coolColor].
 *
 * The cooling is physical: Newton's law of cooling from [peakKelvin] toward room temperature with
 * time constant [coolingSeconds]; the glow fades to nothing at the Draper point (798 K); the glow
 * color at each temperature comes from [colorScale] (blackbody by default).
 *
 * Particle physics runs in seconds, so [cycleMillis] must match the loop duration of the
 * modifier's `animationSpec` ([cyberWeld] wires this up for you).
 *
 * @param width Weld bead width.
 * @param jitter Bead roughness (normal offset amplitude); [jitterStep] is its spacing.
 * @param cycleMillis Duration of one pass round the outline.
 * @param coolingSeconds Cooling time constant; larger leaves a longer glowing trail.
 * @param peakKelvin Bead temperature right behind the molten pool.
 * @param bands Number of color bands the cooling trail is drawn in.
 * @param fizzRate Small sparks emitted per second.
 * @param popSlotSeconds, popChance Pops are decided per time slot: each slot pops with this chance.
 * @param colorScale (kelvin, color) stops for glowing metal and sparks. Defaults to blackbody.
 * @param coolColor Color of fully cooled metal. Defaults to Void100, the palette token closest to gunmetal.
 * @param arcColor, arcHaloColor Arc core and halo colors.
 */
data class CyberWeld(
    val width: Dp = CyberPathDefaults.Weld.Width,
    val jitter: Dp = CyberPathDefaults.Weld.Jitter,
    val jitterStep: Dp = CyberPathDefaults.Weld.JitterStep,
    val cycleMillis: Int = 4000,
    val coolingSeconds: Float = 0.9f,
    val peakKelvin: Float = 3000f,
    val bands: Int = 18,
    val fizzRate: Float = 200f,
    val popSlotSeconds: Float = 0.18f,
    val popChance: Float = 0.3f,
    val colorScale: List<Pair<Float, Color>> = CyberPathDefaults.Weld.Blackbody,
    val coolColor: Color = CyberPrimitives.Colors.Void100,
    val arcColor: Color = CyberPathDefaults.Weld.ArcCore,
    val arcHaloColor: Color = CyberPathDefaults.Weld.ArcHalo,
) : CyberPathEffect {
    private val stops by lazy { colorScale.sortedBy { it.first } }

    override fun extent(density: Density) = with(density) { width.toPx() * 2.25f } // arc halo reach

    private fun ageAt(kelvin: Float): Float =
        -coolingSeconds * ln((kelvin - AMBIENT_KELVIN) / (peakKelvin - AMBIENT_KELVIN))

    private fun emission(kelvin: Float): Float =
        ((kelvin - DRAPER_KELVIN) / (peakKelvin - DRAPER_KELVIN)).coerceIn(0f, 1f).pow(0.7f) // perceptual fade-out

    /** Jittered polyline along the outline: the weld bead. Stable across frames (no per-frame randomness). */
    private fun seam(outline: Path, closed: Boolean, density: Density): Path = with(density) {
        val m = PathMeasure().apply { setPath(outline, false) }
        val len = m.length
        val n = max(8, (len / jitterStep.toPx()).roundToInt())
        val amp = jitter.toPx()
        val count = if (closed) n else n + 1
        Path().apply {
            for (i in 0 until count) {
                val d = min(len * i / n, len)
                val pos = m.getPosition(d)
                val tan = m.getTangent(d)
                val off = (rand(i, 1) - 0.5f) * 2f * amp
                val x = pos.x - tan.y * off
                val y = pos.y + tan.x * off
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            if (closed) close()
        }
    }

    override fun prepare(outline: Path, closed: Boolean, density: Density): CyberPathRenderer = with(density) {
        val seam = seam(outline, closed, density)
        val sm = PathMeasure().apply { setPath(seam, closed) }
        val len = sm.length
        val cycle = cycleMillis / 1000.0
        val ageMax = ageAt(DRAPER_KELVIN)
        // open lines: the arc exits and its trail cools off the end before the loop restarts
        val span = if (closed) len.toDouble() else len / max(0.05, 1.0 - ageMax / cycle)
        val speed = span / cycle
        val w = width.toPx()
        val temps = FloatArray(bands + 1) { k -> peakKelvin - k * (peakKelvin - DRAPER_KELVIN) / bands }
        val dists = FloatArray(bands + 1) { k -> (speed * ageAt(temps[k])).toFloat() }
        val bandColors = List(bands) { k -> colorAtKelvin(stops, (temps[k] + temps[k + 1]) / 2f) }
        val bandEmission = FloatArray(bands) { k -> emission((temps[k] + temps[k + 1]) / 2f) }
        val nFizz = max(1, (fizzRate * cycle).roundToInt())
        val nSlots = max(1, (cycle / popSlotSeconds).roundToInt())
        val slot = cycle / nSlots
        val gravity = CyberPathDefaults.Weld.Gravity.toPx()
        val pool = CyberPathDefaults.Weld.Pool.toPx()
        val arcHalf = CyberPathDefaults.Weld.ArcHalfLength.toPx()
        val poolColor = colorAtKelvin(stops, peakKelvin)
        val burstR = CyberPathDefaults.Weld.BurstRadius.toPx()
        val burstGrow = CyberPathDefaults.Weld.BurstGrowth.toPx()
        // spark classes: width, color (from the scale at a representative temperature), alpha, hot
        val fizzHot = SparkStyle(CyberPathDefaults.Weld.FizzWidth.toPx(), colorAtKelvin(stops, 3000f), 1f, 0.5f)
        val fizzWarm = SparkStyle(CyberPathDefaults.Weld.FizzWarmWidth.toPx(), colorAtKelvin(stops, 2000f), 0.9f, 0f)
        val fizzCool = SparkStyle(CyberPathDefaults.Weld.FizzCoolWidth.toPx(), colorAtKelvin(stops, 1200f), 0.6f, 0f)
        val popHot = SparkStyle(CyberPathDefaults.Weld.PopWidth.toPx(), colorAtKelvin(stops, 3000f), 1f, 0.5f)
        val popCool = SparkStyle(CyberPathDefaults.Weld.PopCoolWidth.toPx(), colorAtKelvin(stops, 1800f), 0.8f, 0f)

        /** Seam distance of the arc at absolute time [t] seconds (any cycle). */
        fun head(t: Double): Float = ((t / cycle).mod(1.0) * span).toFloat()

        CyberPathRenderer { p ->
            val t = p * cycle
            val s = (p * span).toFloat()
            val frame = floor(p * cycle * 60.0) // 60 Hz brightness flicker
            val flicker = 0.72f + 0.28f * hash01(frame + 5)
            val flare = hash01(frame + 29) < 0.08f
            val out = ArrayList<CyberPathLayer>()
            out.add(CyberPathLayer(null, w, StrokeCap.Round, 1f, 0f, glow = false, path = seam, color = coolColor))
            for (k in 0 until bands) {
                window(len, s - dists[k + 1], dists[k + 1] - dists[k] + 0.5f, len, closed)?.let { pe ->
                    val e = bandEmission[k]
                    // above 2400 K the bead reads overexposed, so it mixes toward white
                    val whiteHot = 0.55f * max(0f, (temps[k] - 2400f) / (peakKelvin - 2400f))
                    out.add(CyberPathLayer(pe, w * (1f + 0.2f * e), StrokeCap.Round, e, whiteHot, glow = e > 0.12f, path = seam, color = bandColors[k]))
                }
            }
            val arcOn = closed || s <= len
            if (arcOn) {
                window(len, s - pool, pool, len, closed)?.let {
                    out.add(CyberPathLayer(it, w * 1.35f, StrokeCap.Round, flicker, 0.85f, path = seam, color = poolColor))
                }
            }

            val fizzHotPath = Path(); val fizzWarmPath = Path(); val fizzCoolPath = Path()
            val popHotPath = Path(); val popCoolPath = Path()
            var fizzHotN = 0; var fizzWarmN = 0; var fizzCoolN = 0; var popHotN = 0; var popCoolN = 0
            var popFlash = 0f

            /** Ballistic spark born at [birth]; returns (age, life, origin, direction, speed) or null if not alive. */
            fun spark(birth: Double, index: Int, salt: Int, v0: Float, v1: Float, life0: Float, life1: Float, coneDeg: Float): Spark? {
                val age = t - birth
                val life = life0 + (life1 - life0) * rand(index, salt + 1)
                if (age < 0.0 || age > life) return null
                val sb = head(birth)
                if (!closed && sb > len) return null
                val d = sb.coerceIn(0f, len)
                val o = sm.getPosition(d)
                val tan = sm.getTangent(d)
                val ox = tan.y // outward normal: outlines run clockwise, so +normal points inward
                val oy = -tan.x
                val th = ((rand(index, salt + 2) * 2f - 1f) * coneDeg) * PI.toFloat() / 180f
                val dx = ox * cos(th) + tan.x * sin(th)
                val dy = oy * cos(th) + tan.y * sin(th)
                val sp = (v0 + (v1 - v0) * rand(index, salt + 3)).dp.toPx()
                return Spark(age.toFloat(), life, o.x, o.y, dx * sp, dy * sp)
            }

            fun Spark.at(a: Float) = floatArrayOf(x0 + vx * a, y0 + vy * a + 0.5f * gravity * a * a)

            // fizz: steady emission, index repeats every cycle so the loop is seamless
            val kFirst = floor((t - 0.35) * fizzRate).toInt() // 0.35 s covers the longest fizz life
            val kLast = floor(t * fizzRate).toInt()
            for (k in kFirst..kLast) {
                val idx = k.mod(nFizz)
                val sp = spark(k / fizzRate.toDouble(), idx, 10, 90f, 300f, 0.05f, 0.22f, 85f) ?: continue
                val heat = 1f - sp.age / sp.life
                val a = sp.at(max(0f, sp.age - 0.016f)) // 16 ms motion streak
                val b = sp.at(sp.age)
                val path = when {
                    heat > 0.6f -> { fizzHotN++; fizzHotPath }
                    heat > 0.3f -> { fizzWarmN++; fizzWarmPath }
                    else -> { fizzCoolN++; fizzCoolPath }
                }
                path.moveTo(a[0], a[1]); path.lineTo(b[0], b[1])
            }
            // pops: each time slot fires with popChance and throws 5-9 big sparks
            val jFirst = floor((t - 0.9) / slot).toInt() // 0.9 s covers the longest pop life
            val jLast = floor(t / slot).toInt()
            for (j in jFirst..jLast) {
                val jdx = j.mod(nSlots)
                if (rand(jdx, 40) >= popChance) continue
                val tPop = (j + rand(jdx, 41)) * slot
                val sincePop = t - tPop
                if (sincePop >= 0.0 && sincePop < 0.07) popFlash = max(popFlash, (1.0 - sincePop / 0.07).toFloat()) // 70 ms flash
                val count = 5 + (5 * rand(jdx, 42)).toInt()
                for (q in 0 until count) {
                    val idx = jdx * 16 + q
                    val sp = spark(tPop, idx, 50, 200f, 480f, 0.25f, 0.55f, 110f) ?: continue
                    val heat = 1f - sp.age / sp.life
                    val a = sp.at(max(0f, sp.age - 0.022f)) // 22 ms motion streak
                    val b = sp.at(sp.age)
                    if (heat > 0.5f) { popHotN++; popHotPath.moveTo(a[0], a[1]); popHotPath.lineTo(b[0], b[1]) }
                    else { popCoolN++; popCoolPath.moveTo(a[0], a[1]); popCoolPath.lineTo(b[0], b[1]) }
                    if (heat < 0.2f && rand(idx, 61) < 0.5f) { // half the big sparks burst into a small star as they die
                        val rr = burstR + burstGrow * (1f - heat / 0.2f)
                        for (ray in 0 until 4) {
                            val an = ray * PI.toFloat() / 2f + rand(idx, 60) * PI.toFloat()
                            popCoolPath.moveTo(b[0] + cos(an) * rr * 0.4f, b[1] + sin(an) * rr * 0.4f)
                            popCoolPath.lineTo(b[0] + cos(an) * rr, b[1] + sin(an) * rr)
                        }
                        popCoolN++
                    }
                }
            }
            for ((n, path, style) in listOf(
                Triple(fizzHotN, fizzHotPath, fizzHot), Triple(fizzWarmN, fizzWarmPath, fizzWarm), Triple(fizzCoolN, fizzCoolPath, fizzCool),
                Triple(popHotN, popHotPath, popHot), Triple(popCoolN, popCoolPath, popCool),
            )) {
                if (n > 0) out.add(CyberPathLayer(null, style.width, StrokeCap.Round, style.alpha, style.hot, path = path, color = style.color))
            }
            if (arcOn) {
                val boost = max(popFlash, if (flare) 0.6f else 0f)
                window(len, s - arcHalf, 2f * arcHalf, len, closed)?.let { pe ->
                    out.add(CyberPathLayer(pe, w * (5.5f + 3f * boost), StrokeCap.Round, 0.45f * flicker + 0.35f * boost, 0.2f, path = seam, color = arcHaloColor))
                    out.add(CyberPathLayer(pe, w * 1.9f, StrokeCap.Round, min(1f, flicker + 0.3f * boost), 0.8f, path = seam, color = arcColor))
                }
            }
            out
        }
    }

    private class SparkStyle(val width: Float, val color: Color, val alpha: Float, val hot: Float)
    private class Spark(val age: Float, val life: Float, val x0: Float, val y0: Float, val vx: Float, val vy: Float)
}

/**
 * Welds along the component's [shape] outline: see [CyberWeld] for the look. Shortcut for
 * `cyberPathBorder(weld, …)` with a stronger glow and a loop that matches the weld's particle clock.
 *
 * @param weld Weld look and physics.
 * @param shape Outline to weld; match the component's background shape.
 * @param glowRadius Blur radius of the glow. Zero disables glow.
 * @param trigger When the weld runs; see [cyberPathBorder].
 * @param interactionSource Source for HOVER / PRESS / FOCUS triggers.
 * @param hideWhenIdle Draw nothing while the trigger is inactive.
 * @param animationSpec One pass round the outline; defaults to a constant-speed loop of `weld.cycleMillis`.
 */
fun Modifier.cyberWeld(
    weld: CyberWeld = CyberWeld(),
    shape: Shape = RectangleShape,
    glowRadius: Dp = CyberPathDefaults.Weld.Glow,
    trigger: CyberInteractionTrigger = CyberInteractionTrigger.ALWAYS,
    interactionSource: InteractionSource? = null,
    hideWhenIdle: Boolean = false,
    animationSpec: AnimationSpec<Float> = infiniteRepeatable(tween(weld.cycleMillis, easing = LinearEasing), RepeatMode.Restart),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = cyberPathBorderNamed(
    a11yName = "CyberWeld",
    effect = weld,
    color = Color.Unspecified, // every weld layer sets its own color
    shape = shape,
    glowRadius = glowRadius,
    trigger = trigger,
    interactionSource = interactionSource,
    hideWhenIdle = hideWhenIdle,
    animationSpec = animationSpec,
    appendedA11y = appendedA11y,
    customA11y = customA11y,
)
