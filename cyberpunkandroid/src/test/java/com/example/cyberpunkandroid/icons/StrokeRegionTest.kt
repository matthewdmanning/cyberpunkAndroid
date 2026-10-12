package com.example.cyberpunkandroid.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Pure-geometry tests for closed, concave and self-crossing stroke outlines. */
class StrokeRegionTest {

    private fun region(centerline: CyberCenterline, stroke: Double, alignment: CyberStrokeAlignment, scale: Double = 1.0) =
        checkNotNull(CenterlineStroker(centerline.contours, scale, alignment).region(stroke))

    private val square = cyberCenterline(10f, 10f) {
        moveTo(0f, 0f); lineTo(10f, 0f); lineTo(10f, 10f); lineTo(0f, 10f); close()
    }

    @Test
    fun closedLoopsThickenOutsideByDefaultAndKeepTheirInside() {
        val r = region(square, 2.0, CyberStrokeAlignment.Outside)
        assertEquals(1, r.solids.size)
        assertEquals(1, r.holes.size)
        assertEquals(14.0 * 14.0, signedArea(r.solids[0]), 1e-6) // sharp corners keep the right angle
        assertEquals(-100.0, signedArea(r.holes[0]), 1e-6) // the hole is the centerline itself
        assertEquals(-2.0, r.bounds.left, 1e-9)
    }

    @Test
    fun centerAndInsideAlignmentsSplitOrMoveTheStroke() {
        val center = region(square, 2.0, CyberStrokeAlignment.Center)
        assertEquals(144.0, signedArea(center.solids[0]), 1e-6)
        assertEquals(-64.0, signedArea(center.holes[0]), 1e-6)
        val inside = region(square, 2.0, CyberStrokeAlignment.Inside)
        assertEquals(100.0, signedArea(inside.solids[0]), 1e-6)
        assertEquals(-36.0, signedArea(inside.holes[0]), 1e-6)
    }

    @Test
    fun anInsideStrokeThickerThanTheLoopFillsItWithoutBreaking() {
        val r = region(square, 8.0, CyberStrokeAlignment.Inside)
        assertEquals(1, r.solids.size)
        assertEquals(0, r.holes.size)
        assertEquals(100.0, signedArea(r.solids[0]), 1e-6)
    }

    @Test
    fun openSegmentHasButtCaps() {
        val line = cyberCenterline(10f, 1f) { moveTo(0f, 0f); lineTo(10f, 0f) }
        val r = region(line, 2.0, CyberStrokeAlignment.Outside) // open contours ignore the alignment
        assertEquals(20.0, signedArea(r.solids[0]), 1e-6)
        assertEquals(2 * 10.0 + 2 * 2.0, perimeter(r.solids[0]), 1e-6)
    }

    @Test
    fun openCornerKeepsItsAngleWithNoMiterLimit() {
        // A 20° V: the outer tip sits half the stroke / sin(10°) past the centerline tip.
        val half = 10.0 * PI / 180.0
        val v = cyberCenterline(100f, 100f) {
            moveTo(0f, 0f)
            lineTo((50 * cos(half)).toFloat(), (50 * sin(half)).toFloat())
            lineTo(0f, (100 * sin(half)).toFloat())
        }
        val r = region(v, 4.0, CyberStrokeAlignment.Center)
        val tipX = 50 * cos(half)
        val expected = 2.0 / sin(half)
        assertEquals(tipX + expected, r.bounds.right, 1e-3)
        assertEquals(1, r.solids.size)
        assertEquals(0, r.holes.size)
    }

    @Test
    fun rightAngleUnionIsExact() {
        val l = cyberCenterline(10f, 10f) { moveTo(0f, 0f); lineTo(10f, 0f); lineTo(10f, 10f) }
        val r = region(l, 2.0, CyberStrokeAlignment.Center)
        assertEquals(40.0, signedArea(r.solids[0]), 1e-6)
        assertEquals(11.0, r.bounds.right, 1e-9)
        assertEquals(-1.0, r.bounds.top, 1e-9)
    }

    @Test
    fun concaveStarKeepsItsInsideAtEveryThickness() {
        val star = cyberCenterline(100f, 100f) {
            for (k in 0 until 10) {
                val radius = if (k % 2 == 0) 45.0 else 18.0
                val a = -PI / 2 + k * PI / 5
                val x = (50 + radius * cos(a)).toFloat()
                val y = (50 + radius * sin(a)).toFloat()
                if (k == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        val centerArea = abs(signedArea(CenterlineStroker(star.contours, 1.0, CyberStrokeAlignment.Outside).lines(1.0)[0].points))
        for (stroke in listOf(1.0, 6.0, 20.0, 60.0)) {
            val r = region(star, stroke, CyberStrokeAlignment.Outside)
            assertEquals("stroke $stroke", 1, r.solids.size)
            assertEquals("stroke $stroke", 1, r.holes.size)
            assertEquals("stroke $stroke", centerArea, -signedArea(r.holes[0]), 1e-6)
            assertSimple(r)
        }
    }

    @Test
    fun narrowNotchFillsInFromOutside() {
        // A U-shaped loop: the notch is 4 wide, so an 8-wide outside stroke closes its mouth.
        val u = cyberCenterline(30f, 30f) {
            moveTo(0f, 0f); lineTo(13f, 0f); lineTo(13f, 20f); lineTo(17f, 20f); lineTo(17f, 0f)
            lineTo(30f, 0f); lineTo(30f, 30f); lineTo(0f, 30f); close()
        }
        val thin = region(u, 1.0, CyberStrokeAlignment.Outside)
        assertEquals(1, thin.solids.size)
        val thick = region(u, 8.0, CyberStrokeAlignment.Outside)
        assertEquals(1, thick.solids.size)
        assertEquals(1, thick.holes.size)
        assertSimple(thick)
        // The outer edge passes over the notch mouth, 8 above the centerline's top.
        assertEquals(-8.0, thick.bounds.top, 1e-9)
        assertTrue(thick.solids[0].none { it.y > -8.0 + 1e-6 && it.y < 0.0 - 1e-6 && it.x > 13.0 && it.x < 17.0 })
    }

    private val cursiveL = cyberCenterline(24f, 24f) {
        moveTo(4f, 21f)
        cubicTo(9f, 19f, 17f, 10f, 17f, 5f)
        cubicTo(17f, 2f, 13f, 2f, 12f, 5f)
        cubicTo(10f, 11f, 9f, 17f, 10f, 20f)
        cubicTo(11f, 22f, 14f, 21f, 16f, 19f)
    }

    @Test
    fun cursiveLCrossesItselfAndKeepsTheLoopOpenWhenThin() {
        val r = region(cursiveL, 1.0, CyberStrokeAlignment.Center, scale = 10.0)
        assertEquals(1, r.solids.size)
        assertEquals(1, r.holes.size) // the eye of the loop
        assertSimple(r)
    }

    @Test
    fun thickCursiveLClosesItsLoopWithoutBreaking() {
        val r = region(cursiveL, 60.0, CyberStrokeAlignment.Center, scale = 10.0)
        assertEquals(1, r.solids.size)
        assertEquals(0, r.holes.size)
        assertSimple(r)
    }

    @Test
    fun figureEightThickensOutsideEachLobe() {
        val eight = cyberCenterline(40f, 20f) {
            moveTo(20f, 10f)
            cubicTo(30f, 0f, 40f, 0f, 40f, 10f)
            cubicTo(40f, 20f, 30f, 20f, 20f, 10f)
            cubicTo(10f, 0f, 0f, 0f, 0f, 10f)
            cubicTo(0f, 20f, 10f, 20f, 20f, 10f)
            close()
        }
        val r = region(eight, 2.0, CyberStrokeAlignment.Outside, scale = 10.0)
        assertEquals(1, r.solids.size)
        assertEquals(2, r.holes.size)
        assertSimple(r)
    }

    @Test
    fun ringArcKeepsItsRadius() {
        val ring = cyberCenterline(24f, 24f) { moveTo(12f, 2f); arcTo(12f, 12f, 360f); close() }
        val r = region(ring, 4.0, CyberStrokeAlignment.Center, scale = 10.0)
        assertEquals(1, r.solids.size)
        assertEquals(1, r.holes.size)
        // Outer radius 102 and inner radius 98 around the same center, within the flattening tolerance.
        assertEquals(120.0 - 102.0, r.bounds.left, 0.25)
        for (p in r.holes[0]) assertEquals(98.0, (p - Pt(120.0, 120.0)).length(), 0.25)
        for (p in r.solids[0]) assertEquals(102.0, (p - Pt(120.0, 120.0)).length(), 0.25)
    }

    @Test
    fun theCenterlineNeverMovesAndTheSpaceHoldsTheStroke() {
        val stroker = CenterlineStroker(square.contours, 10.0, CyberStrokeAlignment.Outside)
        val fit = checkNotNull(stroker.fit(40.0, 140.0, 140.0))
        assertEquals(20.0, fit.maxStrokeWidth, 1e-3) // 100 + 2 × 20 = 140
        assertEquals(20.0, fit.strokeWidth, 1e-3)
        assertEquals(2.0, fit.load, 1e-3)
        val held = stroker.region(fit.strokeWidth)!!
        assertEquals(-10_000.0, signedArea(held.holes[0]), 1e-3)

        val roomy = stroker.fit(4.0, 1000.0, 1000.0)!!
        assertEquals(4.0, roomy.strokeWidth, 0.0)
        assertTrue(roomy.load < 0.02)
        assertEquals(0.0, stroker.fit(4.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)!!.load, 0.0)
        assertNull(stroker.fit(4.0, 50.0, 50.0)) // the centerline alone doesn't fit
    }

    @Test
    fun overlappingSquaresUnion() {
        val a = listOf(Pt(0.0, 0.0), Pt(10.0, 0.0), Pt(10.0, 10.0), Pt(0.0, 10.0))
        val b = a.map { Pt(it.x + 5, it.y + 5) }
        val c = a.map { Pt(it.x + 20, it.y) }
        val loops = unionOutline(listOf(a, b, c))
        assertEquals(2, loops.size)
        assertEquals(175.0 + 100.0, loops.sumOf { signedArea(it) }, 1e-9)
    }

    @Test
    fun squaresTouchingAtACornerStaySeparate() {
        val a = listOf(Pt(0.0, 0.0), Pt(10.0, 0.0), Pt(10.0, 10.0), Pt(0.0, 10.0))
        val b = a.map { Pt(it.x + 10, it.y + 10) }
        val loops = unionOutline(listOf(a, b))
        assertEquals(2, loops.size)
        assertTrue(loops.all { abs(signedArea(it) - 100.0) < 1e-9 })
    }

    /** No loop edge crosses another loop edge, so every perimeter is a clean trace. */
    private fun assertSimple(r: StrokeRegion) {
        val edges = (r.solids + r.holes).flatMap { loop -> loop.indices.map { loop[it] to loop[(it + 1) % loop.size] } }
        for (i in edges.indices) for (j in i + 1 until edges.size) {
            val (a, b) = edges[i]
            val (c, d) = edges[j]
            val d1 = (d - c).cross(a - c)
            val d2 = (d - c).cross(b - c)
            val d3 = (b - a).cross(c - a)
            val d4 = (b - a).cross(d - a)
            val proper = d1 * d2 < -1e-9 && d3 * d4 < -1e-9
            assertFalse("edges $i and $j cross", proper)
        }
    }
}
