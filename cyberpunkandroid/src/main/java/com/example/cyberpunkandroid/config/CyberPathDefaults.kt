package com.example.cyberpunkandroid.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default dimensions for the path-effect system (`cyberPathBorder` / `cyberPathDivider` and the
 * effects in `CyberPathPatterns.kt` / `CyberPathTracers.kt`). Values were tuned visually on a
 * 250x150dp chamfered card, a 116dp ring, a rounded button and a divider.
 */
object CyberPathDefaults {
    object Modifiers {
        val BorderGlow: Dp = 8.dp
        val DividerGlow: Dp = 6.dp
        val DividerInset: Dp = 0.dp
    }

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

    object Brackets { val Arm: Dp = 12.dp; val Width: Dp = 2.dp }
    object Ticks { val Spacing: Dp = 4.dp; val Minor: Dp = 4.dp; val Major: Dp = 8.dp; val Width: Dp = 1.dp }
    object Barcode { val Module: Dp = 1.2.dp; val Height: Dp = 6.dp }
    object Hazard { val Band: Dp = 5.dp; val Stripe: Dp = 4.dp; val Slant: Dp = 3.dp }
    object Braid {
        val Wavelength: Dp = 22.dp; val Amplitude: Dp = 3.dp; val Hairline: Dp = 0.8.dp
        /** Gap between the weave's crest and each rail. */
        val RailGap: Dp = 1.4.dp
    }
    object BarbedWire {
        val Twist: Dp = 6.dp; val Barb: Dp = 3.2.dp; val Wire: Dp = 0.9.dp
        /** Distance of each twisted strand from the centre line. */
        val StrandOffset: Dp = 1.dp
    }
    object Circuit { val Spacing: Dp = 32.dp; val Width: Dp = 1.2.dp; val Via: Dp = 2.dp; val Stub: Dp = 5.dp; val Pad: Dp = 1.5.dp }
    object Chain { val Link: Dp = 11.dp; val Thickness: Dp = 5.5.dp; val Wire: Dp = 1.3.dp }

    object Comet { val Width: Dp = 2.dp; val Head: Dp = 5.dp }
    object Charge { val Width: Dp = 2.dp; val Head: Dp = 4.dp }
    object DrawOn { val Width: Dp = 2.dp; val Head: Dp = 6.dp }
    object Scanner { val Width: Dp = 2.5.dp; val Head: Dp = 4.dp }
    object LiveWire { val Width: Dp = 1.6.dp; val Jitter: Dp = 2.2.dp; val Segment: Dp = 5.dp }
    object Packets {
        val Width: Dp = 2.dp; val Edge: Dp = 1.5.dp
        val LongPacket: Dp = 20.dp; val MediumPacket: Dp = 9.dp; val ShortPacket: Dp = 4.dp
    }
    object Meter { val Segment: Dp = 7.dp; val Gap: Dp = 1.8.dp; val Height: Dp = 4.dp }
    object Lights { val Size: Dp = 3.5.dp; val Spacing: Dp = 10.dp; val Width: Dp = 1.5.dp }
}
