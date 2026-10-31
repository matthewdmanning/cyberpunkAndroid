package com.example.cyberpunkandroid.config

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CyberConfig {
    object Invariants {
        val ViewBox: Dp = 24.dp
    }

    object Easings {
        val CyberEasing: Easing = CubicBezierEasing(0.77f, 0.0f, 0.175f, 1.0f)
        val BounceEasing: Easing = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
        val OutExpoEasing: Easing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
        val OverloadEasing: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
        val DecelEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        val FloatEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val AccelEasing: Easing = CubicBezierEasing(0.8f, 0.0f, 1.0f, 1.0f)
        val GlowPulseEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
    }

    object Icon {
        const val DuotoneAlpha: Float = 0.4f
        const val OverloadAlpha: Float = 0.8f
        const val OverloadOffset: Float = 2.0f
    }

    object Shaders {
        const val OverloadCoefficient: Float = 0.1f
        const val CrtCurvature: Float = 0.3f
        const val ScanlineOpacity: Float = 0.02f
        const val NoiseOpacity: Float = 0.03f

        // Defaults for CyberPixelFrontierTransition.
        /** Default hardware block size in pixels (2f for visible pixel blocks). */
        const val PixelFrontierBlockSizePx: Float = 2f

        /** Width of the pixelated band, counted in coarsest blocks. Focused tight frontier edge. */
        const val PixelFrontierBandBlocks: Float = 10f

        /** Smallest brightness difference (0..1) between two neighbor blocks that draws an outline. */
        const val PixelFrontierEdgeThreshold: Float = 0.12f

        /** Peak opacity (0..1) of the soft glow drawn along the frontier. 0 turns the glow off. */
        const val PixelFrontierGlowStrength: Float = 0.65f
    }

    object Effects {
        const val GlowPulseDuration: Int = 1000
        const val PulseMinOpacity: Float = 0.2f
        const val GlowIntensity: Float = 3f
        const val PingScale: Float = 2.5f

        /** Peak opacity (0..1) of the tint that [com.example.cyberpunkandroid.effects.cyberChangeFlash] draws when data changes. */
        const val ChangeFlashAlpha: Float = 0.35f

        /** Extra scale (0.15 = 15 %) that [com.example.cyberpunkandroid.effects.cyberChangeBump] adds at the peak of a change pulse. */
        const val ChangeBumpScale: Float = 0.15f

        /** Remaining-time fraction (0..1) below which a countdown indicator shows its warning color. */
        const val CountdownWarningFraction: Float = 0.25f

        /** Vertical position of a leader line in its bounds, as a fraction of the height (0.5 = center). */
        const val LeaderLineCenterFraction: Float = 0.5f

        /** Holographic drift speed in palette cycles per second. 0.1 is one full cycle every 10 s: visible, not busy. */
        const val HoloDriftCyclesPerSecond: Float = 0.1f

        /** Number of full palette cycles across a holographic brush. 1 shows each palette color once. */
        const val HoloBands: Float = 1f

        /** Direction of holographic color bands, in degrees clockwise from the +x axis. 30 gives a shallow diagonal. */
        const val HoloAngleDegrees: Float = 30f

        /** Peak opacity (0..1) of a projector beam at its source edge. */
        const val BeamAlpha: Float = 0.22f

        /** Peak opacity (0..1) of the chromatic echo copies at the start of a change. */
        const val ChromaEchoAlpha: Float = 0.8f
    }
}
