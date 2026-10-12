package com.example.cyberpunkandroid.icons

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/*
 * A cubic Bézier segment as two cubic polynomials, x(t) and y(t), for 0 ≤ t ≤ 1.
 *
 * Each coordinate is a·t³ + b·t² + c·t + d. Its critical points are the zeros of
 * 3a·t² + 2b·t + c, found with the reduced discriminant Δ = b² − 3ac:
 * t = (−b ± √Δ) / (3a). There the curve's tangent is axis-aligned, so its normal line is
 * an axis line. The second derivative's sign at a point says which way the curve bends. For
 * a plane curve that sign is the sign of x′y″ − y′x″ (it reduces to f″ when x = t). The
 * stroke grows on the convex side, away from the center of the bend, so it never fills the
 * inside of a curl.
 *
 * Pure Kotlin in doubles, like StrokeRegion.
 */

/** One coordinate of a cubic Bézier as a polynomial: a·t³ + b·t² + c·t + d. */
internal class CubicPolynomial(val a: Double, val b: Double, val c: Double, val d: Double) {
    fun value(t: Double) = ((a * t + b) * t + c) * t + d
    fun first(t: Double) = (3 * a * t + 2 * b) * t + c
    fun second(t: Double) = 6 * a * t + 2 * b

    /** Reduced discriminant of the derivative's zeros, Δ = b² − 3ac. */
    fun derivativeDiscriminant() = b.pow(2) - 3 * a * c

    /** Parameters in (0, 1) where the derivative is zero: the coordinate's local extrema. */
    fun criticalPoints(): List<Double> {
        if (abs(a) <= PolynomialEpsilon) {
            // The derivative is linear, 2b·t + c: one zero at most.
            return if (abs(b) <= PolynomialEpsilon) emptyList() else listOf(-c / (2 * b)).filter { it in Inside }
        }
        val delta = derivativeDiscriminant()
        return when {
            delta > PolynomialEpsilon -> {
                val root = sqrt(delta)
                listOf((-b + root) / (3 * a), (-b - root) / (3 * a)).filter { it in Inside }
            }
            delta >= -PolynomialEpsilon -> listOf(-b / (3 * a)).filter { it in Inside } // a stationary inflection
            else -> emptyList() // monotonic: no extrema
        }
    }

    companion object {
        /** The polynomial of one coordinate (picked by [axis]) of the Bézier with control points [p]. */
        fun of(p: Array<Pt>, axis: (Pt) -> Double): CubicPolynomial {
            val p0 = axis(p[0]); val p1 = axis(p[1]); val p2 = axis(p[2]); val p3 = axis(p[3])
            return CubicPolynomial(
                a = -p0 + 3 * p1 - 3 * p2 + p3,
                b = 3 * p0 - 6 * p1 + 3 * p2,
                c = -3 * p0 + 3 * p1,
                d = p0
            )
        }
    }
}

/** A cubic Bézier with its x(t) and y(t) polynomials. */
internal class CubicCurve(val points: Array<Pt>) {
    val x = CubicPolynomial.of(points) { it.x }
    val y = CubicPolynomial.of(points) { it.y }

    fun at(t: Double) = Pt(x.value(t), y.value(t))
    fun tangent(t: Double) = Pt(x.first(t), y.first(t))

    /**
     * Signed curvature at [t]: positive where the curve turns toward its left normal
     * (−y′, x′), which is +normal in StrokeRegion. Zero on straight runs and at cusps.
     */
    fun curvature(t: Double): Double {
        val dx = x.first(t); val dy = y.first(t)
        val speed = sqrt(dx * dx + dy * dy)
        if (speed <= PolynomialEpsilon) return 0.0
        return (dx * y.second(t) - dy * x.second(t)) / (speed * speed * speed)
    }

    /**
     * Where the curve should be split so each piece bends one way and is monotonic in x and
     * y: the critical points of x(t) and y(t), where the normal line is an axis line, and
     * the inflections, where x′y″ − y′x″ changes sign. Sorted, in (0, 1).
     */
    fun splitParameters(): List<Double> =
        (x.criticalPoints() + y.criticalPoints() + inflections()).sorted()
            .fold(ArrayList<Double>()) { out, t -> if (out.isEmpty() || t - out.last() > SplitSpacing) out += t; out }

    /** Zeros in (0, 1) of x′y″ − y′x″, a quadratic in t. */
    private fun inflections(): List<Double> {
        // x′ = 3a t² + 2b t + c and x″ = 6a t + 2b (same for y); the t³ terms cancel.
        val qa = 6 * (x.b * y.a - x.a * y.b)
        val qb = 6 * (x.c * y.a - x.a * y.c)
        val qc = 2 * (x.c * y.b - x.b * y.c)
        if (abs(qa) <= PolynomialEpsilon) return if (abs(qb) <= PolynomialEpsilon) emptyList() else listOf(-qc / qb).filter { it in Inside }
        val disc = qb * qb - 4 * qa * qc
        if (disc < 0) return emptyList()
        val root = sqrt(disc)
        return listOf((-qb + root) / (2 * qa), (-qb - root) / (2 * qa)).filter { it in Inside }
    }

    /** Control points of the piece between [t0] and [t1]. */
    fun piece(t0: Double, t1: Double): Array<Pt> {
        val d = (t1 - t0) / 3.0
        val a = at(t0)
        val b = at(t1)
        return arrayOf(a, a + tangent(t0) * d, b - tangent(t1) * d, b)
    }
}

// Coefficients this small count as zero.
private const val PolynomialEpsilon = 1e-12

// Split parameters closer than this merge; the piece between them would be invisible.
private const val SplitSpacing = 1e-6

private val Inside = 1e-9..1 - 1e-9
