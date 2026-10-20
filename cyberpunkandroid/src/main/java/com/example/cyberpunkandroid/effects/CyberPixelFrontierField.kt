package com.example.cyberpunkandroid.effects

import org.apache.commons.statistics.descriptive.Mean
import org.apache.commons.statistics.descriptive.Variance
import org.apache.commons.statistics.distribution.BetaDistribution
import org.apache.commons.statistics.distribution.ContinuousDistribution
import kotlin.math.floor
import kotlin.math.sin

/**
 * Pure mathematical model for 2-D particle spawn densities, top-to-bottom spline extrema curves,
 * 2D isotropic Fourier fields, and completely random lifetimes in [CyberShaders.PixelFrontierShader].
 *
 * Implements:
 * 1. 2-D particle spawn density field governed by top-to-bottom spline curves as extrema of densities
 *    (meandering curves that are not all the same).
 * 2. True 2D isotropic continuous Fourier wave field Phi(x, y, t) with zero directional bias.
 * 3. Completely random particle lifetimes drawn from a defined platykurtic probability distribution
 *    (low peakedness, broad flat shoulders without Gaussian clumping).
 */
internal object CyberPixelFrontierField {

    /**
     * GLSL-compatible 2D pseudo-random hash returning an independent scalar in [0.0, 1.0).
     */
    fun hash2D(x: Float, y: Float, salt: Float = 0f): Float {
        val dot = x * (12.9898f + salt * 3.7f) + y * (78.233f + salt * 7.1f)
        val s = (sin(dot.toDouble()) * 43758.5453).toFloat()
        val f = (s - floor(s.toDouble())).toFloat()
        return f.coerceIn(0f, 0.999999f)
    }

    /**
     * Standard Catmull-Rom cubic spline interpolator.
     */
    fun catmullRom(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val t2 = t * t
        val t3 = t2 * t
        return 0.5f * (
            (2.0f * p1) +
            (-p0 + p2) * t +
            (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * t2 +
            (-p0 + 3.0f * p1 - 3.0f * p2 + p3) * t3
        )
    }

    /**
     * Evaluates a piece-wise Catmull-Rom spline with 6 control points across normalized coordinate v in [0, 1].
     */
    fun evalSpline(p0: Float, p1: Float, p2: Float, p3: Float, p4: Float, p5: Float, v: Float): Float {
        val sv = v.coerceIn(0f, 0.9999f) * 3.0f
        val idx = sv.toInt()
        val t = sv - idx
        return when (idx) {
            0 -> catmullRom(p0, p1, p2, p3, t)
            1 -> catmullRom(p1, p2, p3, p4, t)
            else -> catmullRom(p2, p3, p4, p5, t)
        }
    }

    // Distinct top-to-bottom spline curves acting as density extrema (not all the same).
    // Start offset is 0 so particle spawn density begins immediately at the barrier/divide:
    fun evalSplineStart(v: Float): Float = 0.0f

    fun evalSplinePeak(v: Float): Float =
        evalSpline(0.36f, 0.46f, 0.38f, 0.52f, 0.34f, 0.44f, v)

    fun evalSplineEnd(v: Float): Float =
        evalSpline(0.72f, 0.84f, 0.70f, 0.88f, 0.76f, 0.82f, v)

    /**
     * Computes the 2-D particle spawn density rho(normDist, v).
     *
     * Density extrema are defined by top-to-bottom spline curves:
     * - Rises smoothly from 0 at splineStart(v) to peak density (extrema) at splinePeak(v).
     * - Falls smoothly from peak density to 0 at splineEnd(v).
     *
     * @param normDist Normalized position across the sweep band [0.0 = frontier, 1.0 = solid screen].
     * @param v Normalized perpendicular position from top to bottom [0.0 = top, 1.0 = bottom].
     * @return Particle spawn density in [0.0, 1.0].
     */
    fun computeSpawnDensity(normDist: Float, v: Float): Float {
        val xs = evalSplineStart(v)
        val xp = evalSplinePeak(v)
        val xe = evalSplineEnd(v)

        return when {
            normDist <= xs || normDist >= xe -> 0.0f
            normDist <= xp -> {
                val u = ((normDist - xs) / (xp - xs).coerceAtLeast(1e-4f)).coerceIn(0f, 1f)
                3.0f * u * u - 2.0f * u * u * u
            }
            else -> {
                val w = ((normDist - xp) / (xe - xp).coerceAtLeast(1e-4f)).coerceIn(0f, 1f)
                1.0f - (3.0f * w * w - 2.0f * w * w * w)
            }
        }
    }

    /**
     * Evaluates a continuous, band-limited 2D stochastic field Phi(x, y, t) with true 2D spatial autocorrelation.
     *
     * Wavevectors are symmetrically paired between X and Y:
     * sum(k_x^2) == sum(k_y^2), ensuring R(dx, 0) == R(0, dy) and Streakiness Ratio == 1.0.
     */
    fun evaluate2DField(x: Float, y: Float, time: Float, mode: Int = 0): Float {
        val kBase = when (mode) {
            1 -> 0.16f
            2 -> 0.05f
            else -> 0.08f
        }

        val omegaBase = when (mode) {
            1 -> 4.5f
            2 -> 2.2f
            else -> 1.8f
        }

        var total = 0.0f

        // Octave 1: Hexagonal symmetric wave pairs (weight 0.65)
        total += sin((x * kBase + omegaBase * time + 0.0f).toDouble()).toFloat()
        total += sin((y * kBase + omegaBase * time + 0.0f).toDouble()).toFloat()
        total += sin(((0.8660254f * x + 0.5f * y) * kBase + omegaBase * time + 1.2566f).toDouble()).toFloat()
        total += sin(((0.5f * x + 0.8660254f * y) * kBase + omegaBase * time + 1.2566f).toDouble()).toFloat()
        total += sin(((-0.8660254f * x + 0.5f * y) * kBase + omegaBase * time + 2.5133f).toDouble()).toFloat()
        total += sin(((0.5f * x - 0.8660254f * y) * kBase + omegaBase * time + 2.5133f).toDouble()).toFloat()
        total = (total / 6.0f) * 0.65f

        // Octave 2: Diagonal and axis harmonics (weight 0.35, 2x frequency)
        val kHarm = kBase * 2.0f
        val omegaHarm = omegaBase * 1.6f
        var totalHarm = 0.0f
        totalHarm += sin((x * kHarm + omegaHarm * time + 3.7699f).toDouble()).toFloat()
        totalHarm += sin((y * kHarm + omegaHarm * time + 3.7699f).toDouble()).toFloat()
        totalHarm += sin(((0.7071068f * x + 0.7071068f * y) * kHarm + omegaHarm * time + 0.85f).toDouble()).toFloat()
        totalHarm += sin(((0.7071068f * x - 0.7071068f * y) * kHarm + omegaHarm * time + 5.0265f).toDouble()).toFloat()
        totalHarm += sin(((-0.7071068f * x + 0.7071068f * y) * kHarm + omegaHarm * time + 5.0265f).toDouble()).toFloat()
        total += (totalHarm / 5.0f) * 0.35f

        return total
    }

    /**
     * Defined continuous probability distribution with a skinnier tail:
     * Beta(2, 5) from Apache Commons Statistics has positive skewness and a rapidly decaying
     * upper tail, preventing long strings or lingering particles.
     */
    val lifetimeDistribution: ContinuousDistribution = BetaDistribution.of(2.0, 5.0)

    /**
     * Computes a completely random lifetime for particle at (gridX, gridY).
     *
     * Drawn from a defined continuous probability distribution with a skinnier tail
     * (Beta(2, 5) from Apache Commons Statistics).
     *
     * Every particle draws its lifetime independently; lifetimes are NOT correlated or fixed by row.
     */
    fun particleLifetime(gridX: Int, gridY: Int, mode: Int = 0): Float {
        val u = hash2D(gridX.toFloat(), gridY.toFloat(), salt = mode.toFloat())
            .toDouble()
            .coerceIn(1e-5, 1.0 - 1e-5)
        val betaSample = lifetimeDistribution.inverseCumulativeProbability(u).toFloat()
        // 2x increased lifetime: scaled to [0.30, 2.0]
        return (0.30f + 1.70f * betaSample).coerceIn(0.30f, 2.0f)
    }

    /**
     * Evaluates whether an individual particle at (gridX, gridY) is active (spawned and surviving).
     */
    fun isParticleActive(
        gridX: Int,
        gridY: Int,
        normDist: Float,
        v: Float = 0.5f,
        time: Float = 0f,
        mode: Int = 0,
    ): Boolean {
        val density = computeSpawnDensity(normDist, v)
        if (density <= 0.001f) return false

        val life = particleLifetime(gridX, gridY, mode)
        val emitHash = hash2D(gridX.toFloat() + 0.31f, gridY.toFloat() + 0.73f, salt = 43.17f + mode * 2.1f)
        val threshold = (density * (life * 0.5f) * 0.95f).coerceIn(0f, 1f)
        return emitHash < threshold
    }

    /**
     * Summary of 2D spatial autocorrelation analysis.
     */
    data class AutocorrelationResult(
        val rowLagCorrelation: FloatArray,
        val colLagCorrelation: FloatArray,
        val isotropyRatio: Float,
    )

    fun compute2DAutocorrelation(grid: Array<FloatArray>, maxLag: Int): AutocorrelationResult {
        val height = grid.size
        require(height > 0) { "Grid height must be greater than 0" }
        val width = grid[0].size
        require(width > 0) { "Grid width must be greater than 0" }

        val flatDoubles = DoubleArray(height * width)
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                flatDoubles[idx++] = grid[y][x].toDouble()
            }
        }
        val mean = Mean.of(*flatDoubles).asDouble.toFloat()
        val variance = Variance.of(*flatDoubles).asDouble.toFloat().coerceAtLeast(1e-6f)

        val rowLag = FloatArray(maxLag + 1)
        val colLag = FloatArray(maxLag + 1)

        for (dx in 0..maxLag) {
            var covSum = 0.0
            var pairCount = 0
            for (y in 0 until height) {
                for (x in 0 until (width - dx)) {
                    covSum += (grid[y][x] - mean) * (grid[y][x + dx] - mean)
                    pairCount++
                }
            }
            rowLag[dx] = if (pairCount > 0) (covSum / pairCount / variance).toFloat() else 0f
        }

        for (dy in 0..maxLag) {
            var covSum = 0.0
            var pairCount = 0
            for (y in 0 until (height - dy)) {
                for (x in 0 until width) {
                    covSum += (grid[y][x] - mean) * (grid[y + dy][x] - mean)
                    pairCount++
                }
            }
            colLag[dy] = if (pairCount > 0) (covSum / pairCount / variance).toFloat() else 0f
        }

        var rowSum = 0f
        var colSum = 0f
        for (lag in 1..maxLag) {
            rowSum += kotlin.math.abs(rowLag[lag])
            colSum += kotlin.math.abs(colLag[lag])
        }
        val ratio = if (colSum > 1e-5f) rowSum / colSum else 1.0f

        return AutocorrelationResult(
            rowLagCorrelation = rowLag,
            colLagCorrelation = colLag,
            isotropyRatio = ratio,
        )
    }

    data class StreakinessResult(
        val rowMeansVariance: Float,
        val colMeansVariance: Float,
        val streakinessRatio: Float,
    )

    fun computeDirectionalStreakiness(grid: Array<FloatArray>): StreakinessResult {
        val height = grid.size
        val width = grid[0].size

        val rowMeans = DoubleArray(height) { y ->
            val row = DoubleArray(width) { x -> grid[y][x].toDouble() }
            Mean.of(*row).asDouble
        }

        val colMeans = DoubleArray(width) { x ->
            val col = DoubleArray(height) { y -> grid[y][x].toDouble() }
            Mean.of(*col).asDouble
        }

        val rowVar = Variance.of(*rowMeans).asDouble.toFloat()
        val colVar = Variance.of(*colMeans).asDouble.toFloat()

        val ratio = if (colVar > 1e-6f) rowVar / colVar else 1.0f

        return StreakinessResult(
            rowMeansVariance = rowVar,
            colMeansVariance = colVar,
            streakinessRatio = ratio,
        )
    }
}
