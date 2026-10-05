package com.example.cyberpunkandroid.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.theme.CyberElevation

/**
 * Default values for the radial effect system: the radar sweep, the radial pulse, icon illumination
 * and the particle shower (`CyberRadial.kt`, `CyberRadialModifiers.kt`, `CyberParticleShower.kt`).
 *
 * Every number the radial code uses lives here with a reason, so effect code has no unexplained
 * literals. The values are starting points chosen from the motion ranges in
 * `docs/agents/api-conventions.md`; they have not yet been tuned on a physical device.
 */
object CyberRadialDefaults {

    /** Defaults for `rememberCyberRadarSweep` and `Modifier.cyberRadarSweep`. */
    object Sweep {
        /** Length of the fading trail behind the beam head, in degrees. A quarter turn reads as a classic radar wedge. */
        const val TrailDegrees: Float = 90f

        /** One revolution (or one there-and-back in bounce mode) per 0.6 s, the same loop the path effects use. */
        const val LoopMillis: Int = 600

        /** Thickness of the bright line drawn at the head of the beam. Set the modifier's `headWidth` to 0 to hide it. */
        val HeadWidth: Dp = 1.5.dp

        /** Blur radius of the glow around the head line, matching the path dividers. Zero disables the glow. */
        val GlowRadius: Dp = CyberElevation.level3

        /** Opacity of the wedge at its head. Kept below 1 so the wedge glows over a background rather than covering it. */
        const val WedgeAlpha: Float = 0.6f

        /** Share of white mixed into the head line, so it reads as an over-exposed neon edge. */
        const val HeadHot: Float = 0.6f

        /**
         * Smallest arc, in degrees, around the head that counts as lit when the trail is zero (a line-only sweep),
         * so an icon sitting under the line still glows. Used only for icon illumination.
         */
        const val MinLitDegrees: Float = 6f
    }

    /** Defaults for `rememberCyberRadialPulse` and `Modifier.cyberRadialPulse`. */
    object Pulse {
        /** One pulse per 0.8 s. Ambient loop only: tap feedback must stay under 250 ms, so pass a shorter spec for that. */
        const val LoopMillis: Int = 800

        /** Length of the fading trail behind the ring head (the soft rim of a disc wipe). */
        val Trail: Dp = CyberPrimitives.Spacing.dp24

        /** Thickness of the bright ring drawn at the head. Set the modifier's `ringWidth` to 0 to hide it. */
        val RingWidth: Dp = CyberPrimitives.BorderWidths.dp2

        /** Blur radius of the glow around the ring line, matching the path dividers. Zero disables the glow. */
        val GlowRadius: Dp = CyberElevation.level3

        /** Opacity of the trail at the ring head. */
        const val TrailAlpha: Float = 0.6f

        /** Share of white mixed into the ring line, so it reads as an over-exposed neon edge. */
        const val RingHot: Float = 0.6f

        /** Fraction of a ring's life, 0..1, after which it fades out so it vanishes before reaching its end radius. */
        const val FadeStart: Float = 0.65f

        /** Opacity of the filled area behind the rim of a disc wipe (relative to the trail alpha). */
        const val DiscFill: Float = 0.3f

        /** Rings in flight at once for the sonar style. */
        const val SonarRings: Int = 3
    }

    /** Defaults for `Modifier.cyberRadialIllumination`. */
    object Illumination {
        /** Blur radius of the contour glow around the icon silhouette. */
        val Radius: Dp = 10.dp

        /** Glow strength at full effect intensity (1 = the strength of `cyberTextGlow`'s default). */
        const val MaxIntensity: Float = 1f

        /** Intensities below this are skipped, so an idle icon records no glow layer at all. */
        const val MinVisible: Float = 0.01f
    }

    /** Defaults for `CyberParticleShower`. */
    object Shower {
        /** Comets on the path at once. */
        const val Count: Int = 36

        /** Longest tail as a fraction of the path length. */
        const val Tail: Float = 0.12f

        /** Tail thickness. */
        val Width: Dp = 1.6.dp

        /** Length of the hot head of each comet. */
        val Head: Dp = CyberElevation.level2

        /** Whole-number laps per loop. A comet with speed 3 laps three times per loop, so the loop stays seamless. */
        val Speeds: List<Int> = listOf(1, 2, 3)

        /** Overlapping tail passes shared by all comets of one speed; more passes give a smoother fade. */
        const val Steps: Int = 4

        /** Seed for the deterministic scatter of start positions, speed classes and tail lengths. */
        const val Seed: Int = 11

        /** Opacity of each tail pass. Overlapping passes build the bright-to-dim ramp toward the tail end. */
        const val TailPassAlpha: Float = 0.3f

        /** Shortest comet tail as a fraction of the longest, so tail lengths vary. */
        const val MinTailScale: Float = 0.35f

        /** Opacity of the slowest speed class; faster classes are brighter, which reads as depth. */
        const val SlowestAlpha: Float = 0.55f

        /** Head thickness relative to the tail thickness. */
        const val HeadWidthScale: Float = 1.15f

        /** Share of white mixed into the head, matching the comet tracer. */
        const val HeadHot: Float = 0.75f

        /** Thickness of the optional dim track relative to the tail thickness. */
        const val TrackWidthScale: Float = 0.75f

        /** Multipliers that turn a seed and an index into independent pseudo-random inputs for the hash. */
        const val SeedStride: Double = 131.0
        const val PhaseSalt: Double = 1.0
        const val SpeedSalt: Double = 2.0
        const val TailSalt: Double = 3.0
        const val IndexStride: Double = 7.0
    }
}
