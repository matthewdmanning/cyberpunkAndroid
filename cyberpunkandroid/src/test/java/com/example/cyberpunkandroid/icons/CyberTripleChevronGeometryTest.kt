package com.example.cyberpunkandroid.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CyberTripleChevronGeometryTest {
    @Test
    fun generatesThreeChevronCenterlinesInsideBounds() {
        for (shear in listOf(-1f, -0.25f, 0f, 0.25f, 1f)) {
            val chevrons = tripleChevronPoints(
                width = 240f, height = 80f,
                strokeWidth = 4f, gap = 8f, shear = shear
            )
            assertEquals(3, chevrons.size)
            chevrons.forEach { points ->
                assertEquals(3, points.size)
                assertTrue(points.all { it.x >= 2f && it.x <= 238f })
                assertTrue(points.all { it.y >= 2f && it.y <= 78f })
                assertTrue(points[1].x > points[0].x)
                assertTrue(points[1].x > points[2].x)
            }
        }
    }

    @Test
    fun spacingSeparatesBoundingBoxes() {
        val chevrons = tripleChevronPoints(240f, 80f, 4f, 8f, 0.5f)
        val firstMax = chevrons[0].maxOf { it.x }
        val secondMin = chevrons[1].minOf { it.x }
        assertTrue(secondMin - firstMax >= 8f - 0.001f)
    }

    @Test
    fun shearTiltsEndsInOppositeDirections() {
        val positive = tripleChevronPoints(240f, 80f, 4f, 8f, 0.5f)[0]
        val negative = tripleChevronPoints(240f, 80f, 4f, 8f, -0.5f)[0]
        assertTrue(positive[0].x > positive[2].x)
        assertTrue(negative[0].x < negative[2].x)
    }

    @Test
    fun impossibleDimensionsHaveNoGeometry() {
        assertTrue(tripleChevronPoints(20f, 20f, 8f, 12f, 0f).isEmpty())
        assertTrue(tripleChevronPoints(240f, 3f, 4f, 8f, 0f).isEmpty())
    }
}
