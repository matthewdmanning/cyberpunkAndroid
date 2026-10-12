package com.example.cyberpunkandroid.config

/**
 * Default values for spark motion and cooling.
 *
 * `CyberSparkCooling` and `Modifier.cyberSpark` use these values.
 * Each value has a reason or a source.
 * This file has no Compose types, so JVM tests can use it.
 *
 * Research sources (electric spark kernels in air):
 * - Two Regime Cooling in Flow Induced by a Spark Discharge, arXiv:1909.09126.
 * - Spark Induced Flow in Quiescent Air, Purdue dissertation AAI30504521.
 * - Trajectory, Development, and Temperature of Spark Kernels, AFRL ADA560399.
 *
 * The sources measure microsecond and millisecond events.
 * A screen effect must last hundreds of milliseconds.
 * Thus only the dimensionless ratios come from the sources.
 * The time scale is a screen choice, not a measured value.
 */
object CyberSparkDefaults {

    /** Thermal values for the two-regime cooling model. */
    object Cooling {
        /**
         * Spark temperature at launch, in kelvin.
         * This is the top stop of the blackbody color scale.
         * It also matches the `CyberWeld` default peak.
         */
        const val PeakKelvin: Float = 3000f

        /** Room temperature that sparks cool toward, in kelvin. */
        const val AmbientKelvin: Float = 300f

        /**
         * Solids stop visible glow below this temperature (Draper point).
         * A spark is not visible below this value.
         */
        const val DraperKelvin: Float = 798f

        /**
         * Fraction of excess heat lost in the fast regime.
         * Source: about 50% of hot gas cools in the first millisecond.
         * The source fraction is a gas fraction, not a temperature fraction.
         * This model applies it to excess temperature as an approximation.
         */
        const val FastRegimeFraction: Float = 0.5f

        /** Lowest fast-to-slow cooling rate ratio in the sources. */
        const val FastToSlowRateRatioMin: Float = 2f

        /** Highest fast-to-slow cooling rate ratio in the sources. */
        const val FastToSlowRateRatioMax: Float = 10f

        /**
         * Default fast-to-slow cooling rate ratio.
         * It is the geometric mean of the 2 to 10 source range.
         * It is not tuned on a device.
         */
        val FastToSlowRateRatio: Float =
            kotlin.math.sqrt(FastToSlowRateRatioMin * FastToSlowRateRatioMax)

        /**
         * Slow-regime time constant, in seconds.
         * This is a screen choice, not a source value.
         * With the other defaults, the glow lasts about 0.8 s.
         * The old cyberSpark ember life was 0.5 s to 1.25 s.
         */
        const val SlowTimeConstantSeconds: Float = 0.7f

        /**
         * Exponent for the perceptual glow fade.
         * `CyberWeld` uses the same value for its bead.
         */
        const val EmissionExponent: Float = 0.7f
    }

    /**
     * Motion values for `Modifier.cyberSpark`.
     *
     * Units are "extents": one extent is 45% of the smaller side.
     * Thus the burst scales with the component size.
     */
    object Motion {
        /** Fraction of the smaller side that is one extent. Same as the old ember reach. */
        const val ExtentFraction: Float = 0.45f

        /**
         * Center of the launch cone, in radians.
         * Screen Y points down, so -PI/2 points straight up.
         */
        const val LaunchCenterRadians: Float = (-kotlin.math.PI / 2.0).toFloat()

        /**
         * Half-width of the launch cone, in radians (45 degrees).
         * Source: the trajectory distribution is symmetric.
         * Thus the cone is symmetric about its center.
         */
        const val LaunchHalfSpreadRadians: Float = (kotlin.math.PI / 4.0).toFloat()

        /** Lowest launch speed, in extents per second. */
        const val LaunchSpeedMin: Float = 0.5f

        /** Highest launch speed, in extents per second. */
        const val LaunchSpeedMax: Float = 1.25f

        /**
         * Screen-down gravity, in extents per second squared.
         * Tuned by eye so sparks arc and fall in one glow life.
         * Source: buoyancy is negligible, so there is no upward lift.
         */
        const val Gravity: Float = 1.8f

        /** Lowest per-spark playback rate. Same as the old ember range. */
        const val RateMultiplierMin: Float = 0.8f

        /** Width of the per-spark playback rate range. Same as the old range. */
        const val RateMultiplierRange: Float = 1.2f
    }

    /** Size and color stages for `Modifier.cyberSpark`. */
    object Look {
        /** Lowest ember radius, in pixels, at intensity 1. */
        const val RadiusMinPx: Float = 3f

        /** Width of the random ember radius range, in pixels. */
        const val RadiusRangePx: Float = 3f

        /** Fraction of radius that an ember loses over its glow life. */
        const val RadiusShrink: Float = 0.5f

        /** Halo radius as a multiple of the core radius. */
        const val HaloRadiusScale: Float = 2.5f

        /** Halo opacity as a fraction of the core opacity. */
        const val HaloAlpha: Float = 0.3f

        /** Cooling progress where the theme scale leaves white and warning. */
        const val ThemeStageWhiteEnd: Float = 0.25f

        /** Cooling progress where the theme scale leaves primary and secondary. */
        const val ThemeStagePrimaryEnd: Float = 0.60f

        /** Opacity of the warning color at the end of the theme scale. */
        const val ThemeEndWarningAlpha: Float = 0.5f

        /** Warning color when the caller gives none. Same as the old default. */
        const val FallbackWarningArgb: Long = 0xFFFFB800
    }

    /** Seeds for the per-spark random values. Each salt keeps one property independent. */
    object Salt {
        /** Salt for the per-spark playback rate. */
        const val Rate: Float = 3.1f

        /** Salt for the per-spark start offset. */
        const val Offset: Float = 7.7f

        /** Multiplier that gives each spark its own seed space per epoch. */
        const val SeedStride: Int = 10000
    }
}
