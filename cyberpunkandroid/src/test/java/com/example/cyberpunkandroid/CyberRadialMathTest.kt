package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.utils.AlphaStop
import com.example.cyberpunkandroid.utils.CyberRadialDirection
import com.example.cyberpunkandroid.utils.CyberRadialMath
import com.example.cyberpunkandroid.utils.CyberRadialSector
import com.example.cyberpunkandroid.utils.CyberSweepMode
import com.example.cyberpunkandroid.utils.SweepPose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * JVM tests for the pure math behind the radial effects: sectors, angles, sweep timing, the trail
 * opacity profile and the gradient stops built from it. No Android or Compose runtime is needed.
 */
class CyberRadialMathTest {

    /** Float comparison tolerance for angles and opacities. */
    private val eps = 1e-3f

    /**
     * Evaluates gradient [stops] at [position] the way a gradient shader does: linear between stops,
     * with two stops at one position forming a hard edge.
     */
    private fun alphaAt(stops: List<AlphaStop>, position: Float): Float {
        for (i in 0 until stops.size - 1) {
            val a = stops[i]
            val b = stops[i + 1]
            if (b.position > a.position && position >= a.position && position <= b.position) {
                val t = (position - a.position) / (b.position - a.position)
                return a.alpha + (b.alpha - a.alpha) * t
            }
        }
        return stops.last().alpha
    }

    @Test
    fun `sector span handles full, partial, wrapped and degenerate input`() {
        assertEquals(360f, CyberRadialSector.FullCircle.span, eps)
        assertEquals(90f, CyberRadialSector(0f, 90f).span, eps)
        assertEquals(120f, CyberRadialSector(300f, 60f).span, eps)   // wraps over 12 o'clock
        assertEquals(360f, CyberRadialSector(90f, 90f).span, eps)    // end equal to start is the full circle
        assertEquals(360f, CyberRadialSector(0f, 720f).span, eps)    // never more than a full turn
        assertEquals(360f, CyberRadialSector(Float.NaN, 10f).span, eps)
        assertTrue(CyberRadialSector.FullCircle.isFullCircle)
        assertFalse(CyberRadialSector(0f, 90f).isFullCircle)
    }

    @Test
    fun `angles run clockwise from 12 o'clock and round-trip through polar coordinates`() {
        assertEquals(0f, CyberRadialMath.angleFromTop(0f, -1f), eps)     // up
        assertEquals(90f, CyberRadialMath.angleFromTop(1f, 0f), eps)     // right
        assertEquals(180f, CyberRadialMath.angleFromTop(0f, 1f), eps)    // down
        assertEquals(270f, CyberRadialMath.angleFromTop(-1f, 0f), eps)   // left
        assertEquals(-90f, CyberRadialMath.toComposeDegrees(0f), eps)    // Compose starts at 3 o'clock
        var angle = 0f
        while (angle < 360f) {
            val x = CyberRadialMath.polarX(0f, 10f, angle)
            val y = CyberRadialMath.polarY(0f, 10f, angle)
            assertEquals(angle, CyberRadialMath.angleFromTop(x, y), 0.01f)
            angle += 15f
        }
    }

    @Test
    fun `farthest corner reaches every pixel from any origin`() {
        assertEquals(111.8034f, CyberRadialMath.farthestCorner(100f, 50f, 200f, 100f), eps)
        assertEquals(223.6068f, CyberRadialMath.farthestCorner(0f, 0f, 200f, 100f), eps)
    }

    @Test
    fun `wrap travels the full sector, plus the trail when the sector is partial`() {
        val full = CyberRadialMath.sweepPose(0.5f, 360f, 90f, CyberSweepMode.WRAP, clockwise = true, fades = false)
        assertEquals(180f, full.offset, eps)
        assertTrue(full.movingClockwise)
        val partial = CyberRadialMath.sweepPose(1f, 90f, 30f, CyberSweepMode.WRAP, clockwise = true, fades = false)
        assertEquals(120f, partial.offset, eps)          // trail runs off the end
        val fading = CyberRadialMath.sweepPose(1f, 90f, 30f, CyberSweepMode.WRAP, clockwise = true, fades = true)
        assertEquals(90f, fading.offset, eps)            // a fading beam never needs the extra travel
    }

    @Test
    fun `counter-clockwise travel mirrors the offset`() {
        val ccw = CyberRadialMath.sweepPose(0.25f, 200f, 0f, CyberSweepMode.HOLD, clockwise = false, fades = false)
        assertEquals(150f, ccw.offset, eps)
        assertFalse(ccw.movingClockwise)
    }

    @Test
    fun `hold stops at the end of the sector`() {
        val end = CyberRadialMath.sweepPose(1f, 90f, 30f, CyberSweepMode.HOLD, clockwise = true, fades = false)
        assertEquals(90f, end.offset, eps)
    }

    @Test
    fun `bounce eases at both ends, shortens its trail at each turn and flips direction`() {
        val start = CyberRadialMath.sweepPose(0f, 100f, 30f, CyberSweepMode.BOUNCE, clockwise = true, fades = false)
        assertEquals(0f, start.offset, eps)
        assertEquals(0f, start.tailScale, eps)
        val outbound = CyberRadialMath.sweepPose(0.25f, 100f, 30f, CyberSweepMode.BOUNCE, clockwise = true, fades = false)
        assertEquals(50f, outbound.offset, eps)          // halfway through the outbound leg, eased
        assertEquals(1f, outbound.tailScale, eps)        // full speed, full trail
        assertTrue(outbound.movingClockwise)
        val turn = CyberRadialMath.sweepPose(0.5f, 100f, 30f, CyberSweepMode.BOUNCE, clockwise = true, fades = false)
        assertEquals(100f, turn.offset, eps)
        assertEquals(0f, turn.tailScale, eps)
        val inbound = CyberRadialMath.sweepPose(0.75f, 100f, 30f, CyberSweepMode.BOUNCE, clockwise = true, fades = false)
        assertEquals(50f, inbound.offset, eps)
        assertFalse(inbound.movingClockwise)
        val back = CyberRadialMath.sweepPose(1f, 100f, 30f, CyberSweepMode.BOUNCE, clockwise = true, fades = false)
        assertEquals(0f, back.offset, eps)               // seamless loop
    }

    @Test
    fun `trail distance wraps on a full circle and goes negative ahead of the head on a sector`() {
        val pose = SweepPose(offset = 10f, movingClockwise = true, tailScale = 1f)
        assertEquals(20f, CyberRadialMath.trailDistance(350f, pose, 360f), eps)   // 20 degrees behind, across 12 o'clock
        assertEquals(5f, CyberRadialMath.trailDistance(5f, pose, 90f), eps)
        assertTrue(CyberRadialMath.trailDistance(40f, pose, 90f) < 0f)            // ahead of the head
        val ccw = SweepPose(offset = 10f, movingClockwise = false, tailScale = 1f)
        assertEquals(5f, CyberRadialMath.trailDistance(15f, ccw, 90f), eps)
    }

    @Test
    fun `trail alpha is smooth or banded and zero ahead of the head`() {
        assertEquals(0f, CyberRadialMath.trailAlpha(-1f, 90f, 0), eps)
        assertEquals(1f, CyberRadialMath.trailAlpha(0f, 90f, 0), eps)
        assertEquals(0.5f, CyberRadialMath.trailAlpha(45f, 90f, 0), eps)
        assertEquals(0f, CyberRadialMath.trailAlpha(91f, 90f, 0), eps)
        assertEquals(0.3f, CyberRadialMath.trailAlpha(91f, 90f, 0, fill = 0.3f), eps)   // fill is left behind the trail
        // three visible bands: 1, 2/3, 1/3
        assertEquals(1f, CyberRadialMath.trailAlpha(29f, 90f, 3), eps)
        assertEquals(2f / 3f, CyberRadialMath.trailAlpha(31f, 90f, 3), eps)
        assertEquals(1f / 3f, CyberRadialMath.trailAlpha(89f, 90f, 3), eps)
        assertEquals(1f / 3f, CyberRadialMath.trailAlpha(90f, 90f, 3), eps)             // trail end stays in the last band
        assertEquals(1f, CyberRadialMath.trailAlpha(0f, 0f, 0), eps)                    // zero trail: only the head
    }

    @Test
    fun `edge fade is off on a full circle and ramps in and out on a sector`() {
        assertEquals(1f, CyberRadialMath.edgeFactor(0f, 360f, 20f), eps)
        assertEquals(1f, CyberRadialMath.edgeFactor(0f, 90f, 0f), eps)
        assertEquals(0f, CyberRadialMath.edgeFactor(0f, 90f, 10f), eps)
        assertEquals(0.5f, CyberRadialMath.edgeFactor(5f, 90f, 10f), eps)
        assertEquals(1f, CyberRadialMath.edgeFactor(45f, 90f, 10f), eps)
        assertEquals(0f, CyberRadialMath.edgeFactor(90f, 90f, 10f), eps)
        assertEquals(0f, CyberRadialMath.edgeFactor(120f, 90f, 10f), eps)               // head already outside
    }

    @Test
    fun `gradient stops are ordered, cover 0 to 1 and reproduce the trail profile`() {
        for (behindIsLower in listOf(true, false)) {
            for (head in listOf(0f, 0.1f, 0.5f, 0.9f, 1f)) {
                for (trail in listOf(0f, 0.05f, 0.3f, 1f)) {
                    for (steps in listOf(0, 3, 6)) {
                        for (fill in listOf(0f, 0.3f)) {
                            val stops = CyberRadialMath.trailStops(head, trail, steps, fill, behindIsLower)
                            val label = "head=$head trail=$trail steps=$steps fill=$fill lower=$behindIsLower"
                            assertEquals("first stop $label", 0f, stops.first().position, 0f)
                            assertEquals("last stop $label", 1f, stops.last().position, 0f)
                            for (i in 1 until stops.size) {
                                assertTrue("ordered $label", stops[i].position >= stops[i - 1].position)
                            }
                            assertTrue("alpha range $label", stops.all { it.alpha in 0f..1f })

                            var position = 0.0137f
                            while (position < 1f) {
                                val distance = if (behindIsLower) head - position else position - head
                                val bandPhase = if (trail > 0f && steps > 0) (distance / trail * steps).mod(1f) else 0.5f
                                val nearEdge = abs(distance) < 0.002f || abs(distance - trail) < 0.002f ||
                                    bandPhase < 0.01f || bandPhase > 0.99f
                                if (!nearEdge) {
                                    val expected = if (distance < 0f) 0f else CyberRadialMath.trailAlpha(distance, trail, steps, fill)
                                    // smooth ramps are sampled linearly between stops, bands are constant
                                    assertEquals("profile at $position, $label", expected, alphaAt(stops, position), 2e-3f)
                                }
                                position += 0.0137f
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `sweep stops put the head at the end of the trail for clockwise and at the start for counter-clockwise`() {
        val clockwise = CyberRadialMath.sweepStops(90f, 0, movingClockwise = true)
        assertEquals(0.25f, clockwise.headFraction, eps)
        assertEquals(1f, alphaAt(clockwise.stops, 0.2499f), 0.01f)                      // bright at the head
        assertEquals(0f, alphaAt(clockwise.stops, 0.2501f), eps)                        // hard edge ahead of it
        val counter = CyberRadialMath.sweepStops(90f, 0, movingClockwise = false)
        assertEquals(0f, counter.headFraction, eps)
        assertEquals(1f, alphaAt(counter.stops, 0.0001f), 0.01f)
        assertEquals(0f, alphaAt(counter.stops, 0.2501f), eps)
    }

    @Test
    fun `ring timing is staggered, direction-aware and fades out`() {
        assertEquals(0.2333f, CyberRadialMath.ringProgress(0.9f, 1, 3), eps)
        assertEquals(0.3f, CyberRadialMath.pulseHead(0.3f, CyberRadialDirection.OUTWARD), eps)
        assertEquals(0.7f, CyberRadialMath.pulseHead(0.3f, CyberRadialDirection.INWARD), eps)
        assertEquals(1f, CyberRadialMath.pulseFade(0.5f, 0.65f), eps)
        assertEquals(0.5f, CyberRadialMath.pulseFade(0.825f, 0.65f), eps)
        assertEquals(0f, CyberRadialMath.pulseFade(1f, 0.65f), eps)
        assertEquals(1f, CyberRadialMath.pulseFade(1f, 1f), eps)                        // fade disabled
    }
}

