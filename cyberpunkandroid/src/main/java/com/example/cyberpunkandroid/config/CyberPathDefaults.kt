package com.example.cyberpunkandroid.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default dimensions for the path-effect system (`cyberPathBorder` / `cyberPathDivider` and the
 * effects in `CyberPathPatterns.kt` / `CyberPathTracers.kt`). Values were tuned visually on a
 * 250x150dp chamfered card, a 116dp ring, a rounded button and a divider.
 */
object CyberPathDefaults {
    // TODO: document this
    object Modifiers {
        val BorderGlow: Dp = 8.dp
        val DividerGlow: Dp = 6.dp
        val DividerInset: Dp = 0.dp
    }

    // TODO: document this
    object Geometry {
        /** Sampling step for corner detection. */
        val CornerSample: Dp = 0.5.dp
        /** Turns tighter than this radius count as corners (12dp rounded corners do, big circles don't). */
        val CornerMaxRadius: Dp = 24.dp
        /** Gaps up to this length between turning samples merge into one corner (polygonal arcs). */
        val CornerGapTolerance: Dp = 2.dp
        /** Edge subdivision for Morph stamps so they follow corners. */
        val MorphStep: Dp = 1.dp
        /** Finer subdivision for small, sharp stamps (chevrons) and branch stubs. */
        val FineStep: Dp = 0.5.dp
        val StubStep: Dp = 0.6.dp
    }

    // TODO: document this
    object Brackets { val Arm: Dp = 12.dp; val Width: Dp = 2.dp }
    // TODO: document this
    object Ticks { val Spacing: Dp = 4.dp; val Minor: Dp = 4.dp; val Major: Dp = 8.dp; val Width: Dp = 1.dp }
    // TODO: document this
    object Barcode { val Module: Dp = 1.2.dp; val Height: Dp = 6.dp }
    // TODO: document this
    object Hazard { val Band: Dp = 5.dp; val Stripe: Dp = 4.dp; val Slant: Dp = 3.dp }
    // TODO: document this
    object Braid {
        val Wavelength: Dp = 22.dp; val Amplitude: Dp = 3.dp; val Hairline: Dp = 0.8.dp
        /** Gap between the weave's crest and each rail. */
        val RailGap: Dp = 1.4.dp
    }
    // TODO: document this
    object BarbedWire {
        val Twist: Dp = 6.dp; val Barb: Dp = 3.2.dp; val Wire: Dp = 0.9.dp
        /** Distance of each twisted strand from the centre line. */
        val StrandOffset: Dp = 1.dp
    }
    // TODO: document this
    object Circuit { val Spacing: Dp = 32.dp; val Width: Dp = 1.2.dp; val Via: Dp = 2.dp; val Stub: Dp = 5.dp; val Pad: Dp = 1.5.dp }
    // TODO: document this
    object Chain { val Link: Dp = 11.dp; val Thickness: Dp = 5.5.dp; val Wire: Dp = 1.3.dp }

    // TODO: document this
    object Comet { val Width: Dp = 2.dp; val Head: Dp = 5.dp }
    // TODO: document this
    object Charge { val Width: Dp = 2.dp; val Head: Dp = 4.dp }
    // TODO: document this
    object DrawOn { val Width: Dp = 2.dp; val Head: Dp = 6.dp }
    // TODO: document this
    object Scanner { val Width: Dp = 2.5.dp; val Head: Dp = 4.dp }
    // TODO: document this
    object LiveWire { val Width: Dp = 1.6.dp; val Jitter: Dp = 2.2.dp; val Segment: Dp = 5.dp }
    // TODO: document this
    object Packets {
        val Width: Dp = 2.dp; val Edge: Dp = 1.5.dp
        val LongPacket: Dp = 20.dp; val MediumPacket: Dp = 9.dp; val ShortPacket: Dp = 4.dp
    }
    // TODO: document this
    object Meter { val Segment: Dp = 7.dp; val Gap: Dp = 1.8.dp; val Height: Dp = 4.dp }
    // TODO: document this
    object Lights { val Size: Dp = 3.5.dp; val Spacing: Dp = 10.dp; val Width: Dp = 1.5.dp }

    // TODO: document this
    object Weld {
        /** Weld bead stroke width. */
        val Width: Dp = 2.2.dp
        /** Roughness of the bead: normal offset amplitude and spacing of the jitter points. */
        val Jitter: Dp = 0.55.dp
        val JitterStep: Dp = 1.6.dp
        /** Length of the molten pool just behind the arc. */
        val Pool: Dp = 4.dp
        /** Half-length of the arc spot along the seam. */
        val ArcHalfLength: Dp = 1.5.dp
        /** Screen-down gravity on sparks, in dp per second squared (tuned by eye, not 9.8 m/s² at screen scale). */
        val Gravity: Dp = 1400.dp
        val FizzWidth: Dp = 0.8.dp
        val FizzWarmWidth: Dp = 0.75.dp
        val FizzCoolWidth: Dp = 0.7.dp
        val PopWidth: Dp = 1.4.dp
        val PopCoolWidth: Dp = 1.1.dp
        /** Star burst at the end of a popping spark: inner radius and growth. */
        val BurstRadius: Dp = 0.8.dp
        val BurstGrowth: Dp = 1.6.dp
        /** Recommended weld glow (more than the default border glow: the arc is high intensity). */
        val Glow: Dp = 10.dp

        /** White core of the arc with a slight blue cast. */
        val ArcCore = Color(0xFFF2F8FF)
        /** Blue halo around the arc. */
        val ArcHalo = Color(0xFF7FB2FF)

        /**
         * Blackbody color scale as (kelvin, sRGB) stops, from the vendian.org blackbody table
         * (via temperature.m15y.com). Only published table values are used; in between is interpolated.
         */
        val Blackbody: List<Pair<Float, Color>> = listOf(
            1000f to Color(0xFFFF3800),
            1200f to Color(0xFFFF5300),
            1800f to Color(0xFFFF7E00),
            2000f to Color(0xFFFF8912),
            3000f to Color(0xFFFFB46B),
        )
    }
}
