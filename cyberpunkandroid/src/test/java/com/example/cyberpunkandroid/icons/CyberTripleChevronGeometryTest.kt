package com.example.cyberpunkandroid.icons

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin

class CyberTripleChevronGeometryTest {
    private val tolerance = 0.05f

    @Test
    fun outlinesFillTheirSizeForEveryShear() {
        for (shear in listOf(-1f, -0.25f, 0f, 0.25f, 1f)) {
            for (stroke in listOf(1f, 4f, 12f)) {
                val geometry = tripleChevronGeometry(240f, 96f, stroke, 8f, shear)!!
                assertEquals(3, geometry.outlines.size)
                val all = geometry.outlines.flatten()
                assertEquals(0f, all.minOf { it.x }, tolerance)
                assertEquals(0f, all.minOf { it.y }, tolerance)
                assertEquals(geometry.size.width, all.maxOf { it.x }, tolerance)
                assertEquals(geometry.size.height, all.maxOf { it.y }, tolerance)
            }
        }
    }

    @Test
    fun thinStrokesFillTheDesignBox() {
        val size = tripleChevronGeometry(240f, 96f, 0.01f, 8f, 0.3f)!!.size
        assertEquals(240f, size.width, 0.5f)
        assertEquals(96f, size.height, 0.5f)
    }

    @Test
    fun theBoxGrowsWithTheStroke() {
        val thin = tripleChevronGeometry(240f, 96f, 2f, 8f, 0.3f)!!.size
        val thick = tripleChevronGeometry(240f, 96f, 12f, 8f, 0.3f)!!.size
        assertTrue(thick.width > thin.width)
        assertTrue(thick.height > thin.height)
    }

    @Test
    fun spacingIsTheGapBetweenNeighborsAtEveryThickness() {
        for (stroke in listOf(1f, 6f, 14f)) {
            val outlines = tripleChevronGeometry(240f, 96f, stroke, 8f, 0.5f)!!.outlines
            for (i in 0..1) assertEquals("stroke=$stroke", 8f, distance(outlines[i], outlines[i + 1]), 0.01f)
        }
    }

    @Test
    fun thickerStrokesKeepTheChevronAngles() {
        fun tipAngle(outline: List<Offset>): Double {
            // Outer edge: the top end's outer corner to the tip, and the tip to the bottom end's.
            val tip = outline.maxBy { it.x }
            val top = outline.minBy { it.y }
            val bottom = outline.maxBy { it.y }
            val a = Math.atan2((top.y - tip.y).toDouble(), (top.x - tip.x).toDouble())
            val b = Math.atan2((bottom.y - tip.y).toDouble(), (bottom.x - tip.x).toDouble())
            return abs(a - b)
        }
        val thin = tripleChevronGeometry(240f, 96f, 2f, 8f, 0f)!!.outlines[0]
        val thick = tripleChevronGeometry(240f, 96f, 14f, 8f, 0f)!!.outlines[0]
        assertEquals(tipAngle(thin), tipAngle(thick), 0.01)
    }

    @Test
    fun theAvailableSpaceIsTheLimit() {
        val geometry = tripleChevronGeometry(240f, 96f, 40f, 8f, 0.2f, maxWidth = 260f, maxHeight = 200f)!!
        assertTrue(geometry.load > 1f)
        assertEquals(StrokeLoad.Limit, StrokeLoad.of(geometry.load))
        assertEquals(geometry.maxStrokeWidth, geometry.strokeWidth, 0f)
        assertTrue(geometry.size.width <= 260f)
        assertEquals(260f, geometry.size.width, 0.5f)
    }

    @Test
    fun theInnerCornerIsTheLimitInUnboundedSpace() {
        val geometry = tripleChevronGeometry(240f, 96f, 500f, 8f, 1f)!!
        assertTrue(geometry.load > 1f)
        assertEquals(6, geometry.outlines[0].size) // still a chevron: six corners
        assertTrue(geometry.maxStrokeWidth < 500f)
    }

    @Test
    fun loadGrowsWithThickness() {
        val thin = tripleChevronGeometry(240f, 96f, 2f, 8f, 0.2f, 400f, 300f)!!
        val thick = tripleChevronGeometry(240f, 96f, 16f, 8f, 0.2f, 400f, 300f)!!
        assertEquals(thin.maxStrokeWidth, thick.maxStrokeWidth, 0.01f)
        assertTrue(thick.load > thin.load)
        assertEquals(StrokeLoad.Ok, StrokeLoad.of(0.3f))
        assertEquals(StrokeLoad.Notice, StrokeLoad.of(0.7f))
        assertEquals(StrokeLoad.Warning, StrokeLoad.of(0.9f))
    }

    @Test
    fun impossibleDimensionsHaveNoGeometry() {
        assertNull(tripleChevronGeometry(20f, 20f, 8f, 12f, 0f))
        assertNull(tripleChevronGeometry(0f, 20f, 2f, 1f, 0f))
        assertNull(tripleChevronGeometry(240f, 80f, 4f, 8f, 1.5f))
        assertNull(tripleChevronGeometry(240f, 80f, Float.NaN, 8f, 0f))
        assertNull(tripleChevronGeometry(240f, 80f, 4f, 8f, 0f, maxWidth = 100f))
    }

    @Test
    fun strokeOutlineOfAStraightLineIsARectangle() {
        val outline = StrokeOutline.of(listOf(Offset(0f, 0f), Offset(10f, 0f)), 2f)!!
        assertTrue(outline.valid)
        val contour = outline.contours.single()
        assertEquals(4, contour.size)
        assertEquals(2f * 10f + 2f * 4f, perimeter(contour), 0.001f)
        assertTrue(signedArea(contour) > 0f) // clockwise on screen
    }

    @Test
    fun strokeOutlineCornerSitsHalfOverSineFromTheTip() {
        val half = 3f
        val tipAngle = 60.0 * Math.PI / 180.0
        val arm = 50f
        val dx = (arm * kotlin.math.cos(tipAngle / 2)).toFloat()
        val dy = (arm * sin(tipAngle / 2)).toFloat()
        val tip = Offset(dx, dy)
        val outline = StrokeOutline.of(listOf(Offset(0f, 0f), tip, Offset(0f, 2f * dy)), half)!!
        val outer = outline.contours.single().maxBy { it.x }
        assertEquals(half / sin(tipAngle / 2).toFloat(), outer.x - tip.x, 0.001f)
        assertTrue(outline.valid)
    }

    @Test
    fun strokeOutlineFlagsInnerCornerTrimmingPastASegment() {
        val outline = StrokeOutline.of(listOf(Offset(0f, 0f), Offset(4f, 2f), Offset(0f, 4f)), 3f)!!
        assertFalse(outline.valid)
    }

    @Test
    fun closedStrokeOutlineHasAHoleWoundOppositely() {
        val square = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 10f), Offset(0f, 10f))
        val outline = StrokeOutline.of(square, 1f, closed = true)!!
        assertTrue(outline.valid)
        val (outer, hole) = outline.contours
        assertEquals(144f, abs(signedArea(outer)), 0.001f)
        assertEquals(64f, abs(signedArea(hole)), 0.001f)
        assertTrue(signedArea(outer) > 0f && signedArea(hole) < 0f)
    }

    private fun perimeter(pts: List<Offset>): Float =
        pts.indices.sumOf { i ->
            val a = pts[i]
            val b = pts[(i + 1) % pts.size]
            hypot(b.x - a.x, b.y - a.y).toDouble()
        }.toFloat()

    private fun signedArea(pts: List<Offset>): Float =
        pts.indices.sumOf { i ->
            val p = pts[i]
            val q = pts[(i + 1) % pts.size]
            (p.x * q.y - q.x * p.y).toDouble()
        }.toFloat() / 2f

    private fun distance(a: List<Offset>, b: List<Offset>): Float {
        fun toSegment(p: Offset, s: Offset, e: Offset): Float {
            val d = e - s
            val t = (((p.x - s.x) * d.x + (p.y - s.y) * d.y) / (d.x * d.x + d.y * d.y)).coerceIn(0f, 1f)
            return (p - (s + d * t)).getDistance()
        }
        var best = Float.MAX_VALUE
        for (i in a.indices) for (j in b.indices) {
            best = minOf(best, toSegment(a[i], b[j], b[(j + 1) % b.size]), toSegment(b[j], a[i], a[(i + 1) % a.size]))
        }
        return best
    }
}
