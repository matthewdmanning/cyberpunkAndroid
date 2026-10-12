package com.example.cyberpunkandroid.config

/**
 * Default values for spark motion and cooling.
 *
 * `CyberSparkCooling`, `CyberSparkDrag`, `CyberSparkLaunch` and `Modifier.cyberSpark` use these values.
 * Each value has a reason or a source.
 * This file has no Compose types, so JVM tests can use it.
 *
 * Motion source (steel grinding sparks):
 * - From the microstructure of steels to the explosion of sparks,
 *   Emergent Scientist 3, 1 (2019).
 *
 * Cooling sources (electric spark kernels in air):
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
     * Air drag values for `CyberSparkDrag`.
     *
     * Source: "From the microstructure of steels to the explosion of sparks",
     * Emergent Scientist 3, 1 (2019), Section 2.1, Eqs. 1-3.
     * The study fits real grinding-spark paths with gravity and air drag only.
     */
    object Drag {
        /**
         * Prefactor of Allen's drag law, C = 18.5 / Re^0.6.
         * Source: Eq. 3 of the steel-spark study.
         */
        const val AllenCoefficient: Float = 18.5f

        /**
         * Reynolds exponent of Allen's drag law.
         * Source: Eq. 3 of the steel-spark study.
         */
        const val AllenReynoldsExponent: Float = 0.6f

        /**
         * Air density, in kg/m^3.
         * ISA sea-level standard value. Not from the spark sources.
         */
        const val AirDensity: Float = 1.225f

        /**
         * Dynamic viscosity of air, in Pa*s.
         * ISA sea-level standard value. Not from the spark sources.
         */
        const val AirViscosity: Float = 1.789e-5f

        /**
         * Spark density, in kg/m^3.
         * Typical carbon steel value. Not from the spark sources.
         * The study used carbon steel sparks.
         */
        const val ParticleDensity: Float = 7850f

        /**
         * Longest physical integration step, in seconds.
         * About 1% of the drag time constant at launch (about 24 ms).
         * This keeps the explicit step accurate and stable.
         */
        const val MaxStepSeconds: Float = 2.5e-4f
    }

    /**
     * Launch values for `CyberSparkLaunch`.
     *
     * Source: the steel-spark study, Tables 1 and 2, 75 N row.
     * One row is used so the values agree with each other.
     */
    object Launch {
        /** Mean spark radius, in meters. Source: Table 1, 50 um. */
        const val RadiusMeanMeters: Float = 50e-6f

        /** Standard deviation of spark radius, in meters. Source: Table 1, 10 um. */
        const val RadiusStdMeters: Float = 10e-6f

        /** Mean launch speed, in m/s. Source: Table 1, 40 m/s. */
        const val SpeedMean: Float = 40f

        /** Standard deviation of launch speed, in m/s. Source: Table 1, 9 m/s. */
        const val SpeedStd: Float = 9f

        /**
         * Random samples are cut at this many standard deviations.
         * This keeps radius and speed above zero. It is a model choice.
         */
        const val SigmaLimit: Float = 2f

        /**
         * Distance where the study starts its distance analysis, in meters.
         * Source: only distances beyond 40 cm are analyzed.
         */
        const val AnalysisStartMeters: Float = 0.40f

        /**
         * Decay length of the spark distance distribution, in meters.
         * Source: Table 2, 25.5 cm at 75 N.
         */
        const val DecayLengthMeters: Float = 0.255f

        /**
         * Mean end distance of sparks seen past the analysis start, in meters.
         * An exponential tail past 40 cm has mean 40 cm plus its decay length.
         * One glow life on screen shows the flight to this distance.
         */
        const val ReferenceDistanceMeters: Float = AnalysisStartMeters + DecayLengthMeters
    }

    /**
     * Screen mapping values for `Modifier.cyberSpark`.
     *
     * One extent is a fraction of the smaller side.
     * The reference distance maps to one extent.
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

    /** Seeds for the per-spark random values. Each seed space is separate. */
    object Salt {
        /** Seed offset for the fixed per-spark rate and start offset. */
        const val Schedule: Long = 0x5EED_0001L

        /** Seed offset for the per-launch path values. */
        const val Launch: Long = 0x5EED_0002L

        /** Multiplier that gives each spark its own seed space per launch. */
        const val SeedStride: Long = 10_000L
    }
}
