package com.example.sample.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.config.CyberRadialDefaults
import com.example.cyberpunkandroid.effects.CyberInteractionTrigger
import com.example.cyberpunkandroid.effects.CyberParticleShower
import com.example.cyberpunkandroid.utils.CyberPulseStyle
import com.example.cyberpunkandroid.effects.CyberRadarSweep
import com.example.cyberpunkandroid.utils.CyberRadialDirection
import com.example.cyberpunkandroid.effects.CyberRadialField
import com.example.cyberpunkandroid.effects.CyberRadialOrigin
import com.example.cyberpunkandroid.effects.CyberRadialRegion
import com.example.cyberpunkandroid.utils.CyberRadialSector
import com.example.cyberpunkandroid.utils.CyberSweepMode
import com.example.cyberpunkandroid.effects.cyberPathAlong
import com.example.cyberpunkandroid.effects.cyberPathBorder
import com.example.cyberpunkandroid.effects.cyberRadarSweep
import com.example.cyberpunkandroid.effects.cyberRadialIllumination
import com.example.cyberpunkandroid.effects.cyberRadialPulse
import com.example.cyberpunkandroid.effects.rememberCyberRadarSweep
import com.example.cyberpunkandroid.effects.rememberCyberRadialPulse
import com.example.cyberpunkandroid.components.CyberDialTicks
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.cos
import kotlin.math.sin

// Radius, in dp, of the ring the demo icons sit on, and the size of the demo dial.
private val DialSize = 220.dp
private val IconRingRadius = 78.dp

// Opacity of the dial face ticks, and of an icon at rest (before the sweep lights it).
private const val TickAlpha = 0.6f
private const val IconRestAlpha = 0.55f

// How far the touch-ping pulse reaches, and how far the custom arc sits inside the demo bounds so its glow is not clipped.
private val TouchPingRadius = 140.dp
private val ArcInset = 4.dp

// Start and length of the custom arc, in Compose's angle convention (0 at 3 o'clock, clockwise): a gap centered on the top.
private const val ArcStartDegrees = 150f
private const val ArcSweepDegrees = 240f

// Whole-number loop for the demos' pulses: slower than the library default so the discrete steps are easy to see.
private const val DemoPulseMillis = 2200

/** One icon on the dial: its drawable resource and accessibility description. */
private data class DialIcon(val res: Int, val description: String)

private val dialIcons = listOf(
    DialIcon(CyberIcons.Wifi, "Wifi"),
    DialIcon(CyberIcons.Shield, "Shield"),
    DialIcon(CyberIcons.Cpu, "Processor"),
    DialIcon(CyberIcons.Lock, "Lock"),
    DialIcon(CyberIcons.Eye, "Eye"),
    DialIcon(CyberIcons.Signal, "Signal"),
)

/**
 * Showcase and cookbook for the radial effects: radar sweeps, radial pulses, icon illumination and the
 * particle shower. The `RadarDial` composable below is the pattern for building your own radial component.
 */
@Composable
fun RadialEffectsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberTheme.colors.background)
            .padding(CyberPrimitives.Spacing.dp16),
        verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp16),
    ) {
        item { Section("RADAR SWEEP") }
        item {
            Demo("Full sweep", "Smooth wedge, bright head line, icons light as it passes.") {
                RadarDial(rememberCyberRadarSweep(tailDegrees = 110f))
            }
        }
        item {
            Demo("Stepped, counter-clockwise", "Six visible bands in the trail, travelling the other way.") {
                RadarDial(rememberCyberRadarSweep(tailDegrees = 120f, fadeSteps = 6, clockwise = false))
            }
        }
        item {
            Demo("Line only", "No wedge: just the head line. Icons still catch it.") {
                RadarDial(rememberCyberRadarSweep(tailDegrees = 0f), showWedge = false)
            }
        }
        item {
            Demo("Sector, bounce", "A 120-degree fan centered on 12 o'clock; the beam slows and turns at each edge.") {
                RadarDial(
                    rememberCyberRadarSweep(
                        sector = CyberRadialSector(300f, 60f),
                        mode = CyberSweepMode.BOUNCE,
                        tailDegrees = 50f,
                        fadeSteps = 5,
                    ),
                )
            }
        }
        item {
            Demo("Sector, hold", "Sweeps the right half once per loop and fades out at the end of the sector.") {
                RadarDial(
                    rememberCyberRadarSweep(
                        sector = CyberRadialSector(0f, 180f),
                        mode = CyberSweepMode.HOLD,
                        tailDegrees = 70f,
                        edgeFadeDegrees = 25f,
                    ),
                )
            }
        }
        item {
            Demo("Icons only", "The beam itself is invisible; only the icons glow. The background is untouched.") {
                RadarDial(rememberCyberRadarSweep(tailDegrees = 90f, fadeSteps = 4), showWedge = false, showHead = false)
            }
        }

        item { Section("RADIAL PULSE") }
        item {
            Demo("Ring, outward", "One ring leaves the center and fades before it reaches the edge.") {
                PulseDial(CyberPulseStyle.RING, CyberRadialDirection.OUTWARD)
            }
        }
        item {
            Demo("Disc, outward, stepped", "A filled disc grows from the center with a banded rim.") {
                PulseDial(CyberPulseStyle.DISC, CyberRadialDirection.OUTWARD, fadeSteps = 5)
            }
        }
        item {
            Demo("Sonar", "Three staggered rings in flight at once.") {
                PulseDial(CyberPulseStyle.SONAR, CyberRadialDirection.OUTWARD)
            }
        }
        item {
            Demo("Ring, inward", "The ring starts at the edge and collapses toward the center.") {
                PulseDial(CyberPulseStyle.RING, CyberRadialDirection.INWARD)
            }
        }
        item {
            Demo("Sonar fan", "Sonar limited to the top-right quarter.") {
                PulseDial(CyberPulseStyle.SONAR, CyberRadialDirection.OUTWARD, sector = CyberRadialSector(0f, 90f))
            }
        }

        item { Section("PARTICLE SHOWER") }
        item {
            Demo("On a circle", "Many comets in three speed classes circle the ring; faster ones are brighter.") {
                Box(Modifier.size(DialSize).cyberPathBorder(CyberParticleShower(), shape = CircleShape))
            }
        }
        item {
            Demo("On a custom arc", "Any path works: this one is a 240-degree open arc, so comets enter and leave at its ends.") {
                Box(
                    Modifier
                        .size(DialSize)
                        .cyberPathAlong(
                            effect = CyberParticleShower(count = 48, tail = 0.2f, seed = 3),
                            path = { size -> arcPath(size.minDimension / 2f - ArcInset.toPx(), ArcStartDegrees, ArcSweepDegrees, Offset(size.width / 2f, size.height / 2f)) },
                        ),
                )
            }
        }

        item { Section("COOKBOOK") }
        item {
            Demo("Hold to ping from your finger", "A pulse whose origin follows the touch. See TouchPing below.") {
                TouchPing()
            }
        }
    }
}

/**
 * Cookbook example: a dial built from library parts. The same [sweep] draws the beam and lights the icons.
 * Copy it and change the pieces: swap the background, the icons, the region or the effect.
 *
 * Ingredients:
 * - `rememberCyberRadarSweep` owns the timing (sector, direction, trail, loop) so everything stays in step;
 * - `cyberRadarSweep` draws the beam, clipped to a circle through [CyberRadialRegion];
 * - `cyberRadialIllumination` lights each icon from the same sweep;
 * - `CyberDialTicks` is the dial face.
 *
 * @param sweep The hoisted sweep that drives the beam and the icons.
 * @param showWedge Draw the fading wedge behind the head line.
 * @param showHead Draw the head line.
 */
@Composable
private fun RadarDial(sweep: CyberRadarSweep, showWedge: Boolean = true, showHead: Boolean = true) {
    val primary = CyberTheme.colors.primary
    Box(
        modifier = Modifier
            .size(DialSize)
            .clip(CircleShape)
            .background(CyberTheme.colors.background)
            .cyberRadarSweep(
                sweep = sweep,
                region = CyberRadialRegion(clipShape = CircleShape),
                showWedge = showWedge,
                headWidth = if (showHead) CyberRadialDefaults.Sweep.HeadWidth else 0.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CyberDialTicks(color = primary.copy(alpha = TickAlpha), size = DialSize)
        DialIcons(field = sweep)
    }
}

/**
 * A dial with a radial pulse and the icons lit by it, in the same pattern as [RadarDial].
 *
 * @param style Ring, disc or sonar.
 * @param direction Outward from the center, or inward from the edge.
 * @param fadeSteps Opacity bands in the trail; 0 is smooth.
 * @param sector Angular slice the pulse covers.
 */
@Composable
private fun PulseDial(
    style: CyberPulseStyle,
    direction: CyberRadialDirection,
    fadeSteps: Int = 0,
    sector: CyberRadialSector = CyberRadialSector.FullCircle,
) {
    val pulse = rememberCyberRadialPulse(
        style = style,
        direction = direction,
        fadeSteps = fadeSteps,
        sector = sector,
        animationSpec = infiniteRepeatable(tween(DemoPulseMillis, easing = LinearEasing), RepeatMode.Restart),
    )
    Box(
        modifier = Modifier
            .size(DialSize)
            .clip(CircleShape)
            .background(CyberTheme.colors.background)
            .cyberRadialPulse(pulse, region = CyberRadialRegion(clipShape = CircleShape)),
        contentAlignment = Alignment.Center,
    ) {
        CyberDialTicks(color = CyberTheme.colors.primary.copy(alpha = TickAlpha), size = DialSize)
        DialIcons(field = pulse)
    }
}

/**
 * Cookbook example: a pulse that starts where you press. Hold to run it; it stops on release.
 *
 * Ingredients: a custom [CyberRadialOrigin] that reads the touch position, a `PRESS` trigger fed by a
 * `MutableInteractionSource` that the pointer handler emits into, and the pulse's `region`.
 */
@Composable
private fun TouchPing() {
    val interactions = remember { MutableInteractionSource() }
    var touch by remember { mutableStateOf(Offset.Unspecified) }
    val pulse = rememberCyberRadialPulse(
        style = CyberPulseStyle.SONAR,
        trigger = CyberInteractionTrigger.PRESS,
        interactionSource = interactions,
        hideWhenIdle = true,
        animationSpec = infiniteRepeatable(tween(DemoPulseMillis, easing = LinearEasing), RepeatMode.Restart),
    )
    val origin = CyberRadialOrigin { size ->
        if (touch.isSpecified) touch else Offset(size.width / 2f, size.height / 2f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .size(DialSize)
            .background(CyberTheme.colors.background)
            .cyberRadialPulse(pulse, region = CyberRadialRegion(origin = origin, outerRadius = TouchPingRadius))
            .pointerInput(Unit) {
                detectTapGestures(onPress = { position ->
                    touch = position
                    val press = PressInteraction.Press(position)
                    interactions.emit(press)
                    tryAwaitRelease()
                    interactions.emit(PressInteraction.Release(press))
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("HOLD", style = CyberTheme.typography.terminal, color = CyberTheme.colors.textSecondary)
    }
}

/**
 * The demo icons, spaced evenly on a ring around the dial center and each lit by [field].
 *
 * @param field The sweep or pulse that lights the icons.
 */
@Composable
private fun DialIcons(field: CyberRadialField) {
    val primary = CyberTheme.colors.primary
    dialIcons.forEachIndexed { index, icon ->
        val degrees = 360f * index / dialIcons.size // clockwise from 12 o'clock, the library's convention
        val radians = Math.toRadians(degrees.toDouble())
        CyberIcon(
            iconRes = icon.res,
            contentDescription = icon.description,
            modifier = Modifier
                .offset(x = IconRingRadius * sin(radians).toFloat(), y = -IconRingRadius * cos(radians).toFloat())
                .cyberRadialIllumination(field, color = primary),
            tint = primary.copy(alpha = IconRestAlpha),
        )
    }
}

/**
 * An arc path for the custom-path demo.
 *
 * @param radius Radius of the arc in pixels.
 * @param startDegrees Where the arc starts, in Compose's angle convention (0 at 3 o'clock, clockwise).
 * @param sweepDegrees Length of the arc in degrees.
 * @param center Center of the arc in pixels.
 */
private fun arcPath(radius: Float, startDegrees: Float, sweepDegrees: Float, center: Offset): Path =
    Path().apply { addArc(Rect(center, radius), startDegrees, sweepDegrees) }

@Composable
private fun Section(text: String) {
    Text(
        text = text,
        style = CyberTheme.typography.display.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold),
        color = CyberTheme.colors.primary,
    )
}

/**
 * A titled demo card: a name, a one-line description and the demo content centered below.
 *
 * @param name Short title.
 * @param description What to look for.
 * @param content The demo.
 */
@Composable
private fun Demo(name: String, description: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp8)) {
        Text(name, style = CyberTheme.typography.terminal, color = CyberTheme.colors.primary)
        Text(description, style = CyberTheme.typography.body.copy(fontSize = 13.sp), color = CyberTheme.colors.textSecondary)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
    }
}
