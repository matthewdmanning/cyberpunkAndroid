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
    fun anInsideStrokeThickerThanTheLoopKeepsPartOfItOpen() {
        // Opposite sides are 10 apart, so each may fill (1 - 0.25) / 2 of that: 3.75.
        val r = region(square, 8.0, CyberStrokeAlignment.Inside)
        assertEquals(1, r.solids.size)
        assertEquals(1, r.holes.size)
        assertEquals(100.0, signedArea(r.solids[0]), 1e-6)
        assertEquals(-2.5 * 2.5, signedArea(r.holes[0]), 1e-6)
        // With no gap kept, the strokes meet and fill the loop.
        val closed = checkNotNull(CenterlineStroker(square.contours, 1.0, CyberStrokeAlignment.Inside, minGap = 0.0).region(8.0))
        assertEquals(0, closed.holes.size)
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
        // Each arm's perpendicular meets the other arm on the inside, so the whole stroke goes outside.
        val expected = 4.0 / sin(half)
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
    fun narrowNotchStaysOpenAsTheStrokeThickens() {
        // A U-shaped loop: the notch is 4 wide, so an 8-wide outside stroke would close it.
        val u = cyberCenterline(30f, 30f) {
            moveTo(0f, 0f); lineTo(13f, 0f); lineTo(13f, 20f); lineTo(17f, 20f); lineTo(17f, 0f)
            lineTo(30f, 0f); lineTo(30f, 30f); lineTo(0f, 30f); close()
        }
        val thick = region(u, 8.0, CyberStrokeAlignment.Outside)
        assertEquals(1, thick.solids.size)
        assertEquals(1, thick.holes.size)
        assertSimple(thick)
        assertEquals(-8.0, thick.bounds.top, 1e-9)
        // Each wall fills 1.5 of the 4-wide notch, so the outer edge runs down into it.
        val inNotch = thick.solids[0].filter { it.x > 13.0 && it.x < 17.0 && it.y > 0.0 }
        assertTrue(inNotch.isNotEmpty())
        assertEquals(14.5, inNotch.minOf { it.x }, 1e-6)
        assertEquals(15.5, inNotch.maxOf { it.x }, 1e-6)
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
    fun thickCursiveLKeepsItsLoopOpen() {
        for (stroke in listOf(20.0, 40.0, 60.0, 90.0)) {
            val r = region(cursiveL, stroke, CyberStrokeAlignment.Center, scale = 10.0)
            assertEquals("stroke $stroke", 1, r.solids.size)
            assertEquals("stroke $stroke", 1, r.holes.size)
            assertSimple(r)
        }
    }

    @Test
    fun crossingLinesStaySolidWhereTheyCross() {
        val x = cyberCenterline(100f, 100f) { moveTo(0f, 50f); lineTo(100f, 50f); moveTo(50f, 0f); lineTo(50f, 100f) }
        val r = region(x, 10.0, CyberStrokeAlignment.Center)
        assertEquals(1, r.solids.size)
        assertEquals(1000.0 + 1000.0 - 100.0, signedArea(r.solids[0]), 1e-6)
    }

    @Test
    fun straightLinesFillAwayFromTheNearestPerpendicularHit() {
        // An open U: each wall's perpendicular meets the opposite wall, so walls thicken outward.
        val u = cyberCenterline(20f, 20f) { moveTo(0f, 0f); lineTo(0f, 20f); lineTo(20f, 20f); lineTo(20f, 0f) }
        val r = region(u, 4.0, CyberStrokeAlignment.Center)
        assertEquals(-4.0, r.bounds.left, 1e-6)
        assertEquals(24.0, r.bounds.right, 1e-6)
        assertFalse(inside(Pt(1.0, 10.0), r)) // nothing filled inside the walls
        // The bottom's perpendicular meets nothing, so it takes the walls' polarity: outward too.
        assertEquals(24.0, r.bounds.bottom, 1e-6)
        assertFalse(inside(Pt(10.0, 19.5), r))
        assertTrue(inside(Pt(10.0, 23.5), r))
        // A lone line has no outside: it stays centered.
        val line = region(cyberCenterline(10f, 1f) { moveTo(0f, 0f); lineTo(10f, 0f) }, 2.0, CyberStrokeAlignment.Center)
        assertEquals(-1.0, line.bounds.top, 1e-9)
    }

    @Test
    fun bothSidesOfEveryJoinShareOneLean() {
        val shapes = listOf(
            cyberCenterline(24f, 24f) { moveTo(6f, 2f); lineTo(6f, 14f); arcTo(14f, 18f, -150f) }, // line into an arc at a corner
            cyberCenterline(24f, 24f) { moveTo(2f, 20f); lineTo(10f, 20f); cubicTo(14f, 8f, 18f, 2f, 22f, 4f); lineTo(22f, 16f) },
            cyberCenterline(24f, 12f) { moveTo(0f, 10f); lineTo(6f, 2f); lineTo(12f, 10f); lineTo(18f, 2f); lineTo(24f, 10f) },
            cursiveL
        )
        for ((index, shape) in shapes.withIndex()) for (stroke in listOf(4.0, 18.0)) {
            val stroker = CenterlineStroker(shape.contours, 10.0, CyberStrokeAlignment.Center)
            for (line in crowdedLines(stroker.lines(stroke), stroke, DefaultMinGap)) {
                val (leanA, leanB) = convexLean(line, stroke)
                for (v in 1 until leanA.size) {
                    assertEquals("shape $index stroke $stroke vertex $v", leanB[v - 1], leanA[v], 1e-9)
                }
            }
        }
    }

    @Test
    fun lineToCurveCornersKeepTheirAngle() {
        // With both sides of each corner sharing one lean, their outer offsets match, so every
        // corner is a miter (four points) and none falls back to a bevel (three points).
        val shapes = listOf(
            cyberCenterline(24f, 24f) { moveTo(6f, 2f); lineTo(6f, 14f); arcTo(14f, 18f, -150f) },
            cyberCenterline(24f, 24f) { moveTo(2f, 20f); lineTo(10f, 20f); cubicTo(14f, 8f, 18f, 2f, 22f, 4f); lineTo(22f, 16f) }
        )
        for (shape in shapes) for (stroke in listOf(4.0, 18.0)) {
            val stroker = CenterlineStroker(shape.contours, 10.0, CyberStrokeAlignment.Center)
            assertTrue(stroker.pieces(stroke).none { it.size == 3 })
            val r = region(shape, stroke, CyberStrokeAlignment.Center, scale = 10.0)
            assertEquals(1, r.solids.size)
            assertSimple(r)
        }
    }

    @Test
    fun tightCornerKeepsAGapOnItsInside() {
        // A 20° V with a stroke far wider than the space between its arms near the tip.
        val half = 10.0 * PI / 180.0
        val v = cyberCenterline(100f, 100f) {
            moveTo(0f, 0f)
            lineTo((50 * cos(half)).toFloat(), (50 * sin(half)).toFloat())
            lineTo(0f, (100 * sin(half)).toFloat())
        }
        val r = region(v, 12.0, CyberStrokeAlignment.Center)
        val bisectorY = 50 * sin(half)
        for (x in listOf(10.0, 25.0, 40.0)) assertFalse("x $x", inside(Pt(x, bisectorY), r))
        // The outside of the corner still keeps the centerline's angle.
        assertEquals(50 * cos(half) + 12.0 / sin(half), r.bounds.right, 1e-3)
    }

    @Test
    fun curvesLeanToTheirConvexSide() {
        // An open half circle, radius 10, stroked 30 wide: tighter than the stroke, so all of
        // it goes outside and the inside of the bend stays empty.
        val arc = cyberCenterline(40f, 40f) { moveTo(30f, 20f); arcTo(20f, 20f, 180f) }
        val r = region(arc, 30.0, CyberStrokeAlignment.Center)
        for (p in r.solids[0]) assertTrue("$p", (p - Pt(20.0, 20.0)).length() >= 10.0 - 0.25)
        // A gentle bend (radius 100, stroke 4) shifts out by only 4/100 of the stroke.
        val wide = region(cyberCenterline(400f, 400f) { moveTo(300f, 200f); arcTo(200f, 200f, 90f) }, 4.0, CyberStrokeAlignment.Center)
        assertEquals(300.0 + 4.0 * 1.04 / 2, wide.bounds.right, 0.05)
    }

    @Test
    fun cubicsSplitAtCriticalPointsAndInflections() {
        val arch = CubicCurve(arrayOf(Pt(0.0, 0.0), Pt(0.0, 10.0), Pt(10.0, 10.0), Pt(10.0, 0.0)))
        assertEquals(listOf(0.5), arch.splitParameters().map { Math.round(it * 1e9) / 1e9 })
        assertTrue(arch.curvature(0.5) != 0.0)
        val s = CubicCurve(arrayOf(Pt(0.0, 0.0), Pt(10.0, 0.0), Pt(0.0, 10.0), Pt(10.0, 10.0)))
        assertTrue(s.splitParameters().any { abs(it - 0.5) < 1e-9 }) // the inflection
        assertTrue(s.curvature(0.25) * s.curvature(0.75) < 0.0)
        // The uploaded example f(x) = x³ − 3x + 2 has extrema at x = ±1 (Δ = 9).
        val f = CubicPolynomial(1.0, 0.0, -3.0, 2.0)
        assertEquals(9.0, f.derivativeDiscriminant(), 0.0)
        assertEquals(0.0, f.first(1.0), 0.0)
        assertTrue(f.second(1.0) > 0.0 && f.second(-1.0) < 0.0)
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

    /** Inside a solid and outside every hole. */
    private fun inside(p: Pt, r: StrokeRegion): Boolean {
        fun winding(loop: List<Pt>): Int {
            var w = 0
            for (i in loop.indices) {
                val a = loop[i]
                val b = loop[(i + 1) % loop.size]
                val side = (b - a).cross(p - a)
                if (a.y <= p.y) { if (b.y > p.y && side > 0) w++ } else if (b.y <= p.y && side < 0) w--
            }
            return w
        }
        return (r.solids + r.holes).sumOf { winding(it) } != 0
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
