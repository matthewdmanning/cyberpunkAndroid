package com.example.sample.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberSlider
import com.example.cyberpunkandroid.components.CyberSwitch
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Design: simulation step for dwell time, in milliseconds. Ten steps per second keep the score smooth. */
private const val SIM_TICK_MILLIS = 100L

/**
 * Design: idle time, in milliseconds, before the depth thumb snaps into its detent.
 * It is under the 250 ms limit for functional feedback in `docs/agents/api-conventions.md`.
 */
private const val DETENT_SNAP_DELAY_MILLIS = 150L

/** Milliseconds in one second. */
private const val MILLIS_PER_SECOND = 1_000f

/**
 * Design: Black ICE blink rate, in flashes per second.
 * The dossier asks for an 8 Hz strobe. WCAG 2.2 SC 2.3.1 allows at most three
 * flashes in any one second, so the card blinks at 2 Hz.
 */
private const val BLACK_ICE_BLINK_HZ = 2f

/** Alpha of a Black ICE block during the dark half of a blink. */
private const val BLINK_DIM_ALPHA = 0.35f

/** Design: horizontal positions of the five topology nodes, as a fraction of the screen width. */
private val NODE_X = listOf(0.08f, 0.3f, 0.52f, 0.74f, 0.92f)

/** Design: vertical positions of the five topology nodes, as a fraction of the screen height. */
private val NODE_Y = listOf(0.5f, 0.28f, 0.68f, 0.35f, 0.6f)

/** Design: radius of a topology node, as a fraction of the screen height. */
private const val NODE_RADIUS_FRACTION = 0.06f

/**
 * Design: largest radius of the trace-lock reticle, as a fraction of the screen height.
 * The drawn radius is smaller when the entry node is close to an edge.
 */
private const val RETICLE_RADIUS_FRACTION = 0.2f

/** Format for one-decimal readouts. */
private const val ONE_DECIMAL = "%.1f"

/** Format for two-decimal readouts. */
private const val TWO_DECIMALS = "%.2f"

/**
 * Roadmap card 7, Level 3: the Arasaka IDM-800 intrusion detection matrix.
 *
 * Source: `docs/inspirations/ice-detection-matrix-countermeasures.md`. The card has two tabs:
 *
 * - **Display:** the intrusion topology screen, the 20-block perimeter ICE bar,
 *   the stepped penetration slider, the stealth rocker, and the two-step trace purge.
 * - **Backbone:** the security coprocessor equation, the detection bands, the
 *   stealth filter, the siren, the haptic motors, the optical relays, and the shunt.
 *
 * A status strip above the tabs always shows the threat badge and the score, so a
 * trace lock is visible from both tabs. TODO(audio): the piezo siren tones (dossier §3).
 *
 * @param modifier Modifier applied to the card.
 */
@Composable
fun IceMatrixCard(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(IceMatrixState()) }
    val motionEnabled = rememberSystemMotionEnabled()
    val clock = rememberShowcaseClockSeconds(running = motionEnabled && state.traceLocked)
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state.connected) {
        while (state.connected) {
            delay(SIM_TICK_MILLIS)
            state = state.advancedBy(SIM_TICK_MILLIS / MILLIS_PER_SECOND)
        }
    }
    LaunchedEffect(state.traceLocked) {
        // One short haptic event at lock-on. A continuous rumble goes against Android haptics guidance.
        if (state.traceLocked) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Text(
                text = "ICE DETECTION MATRIX",
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.primary,
            )
        },
    ) {
        ShowcaseTabbedPanel(
            modifier = Modifier.padding(CyberTheme.spacing.dp16),
            statusStrip = { IceStatusStrip(state) },
            display = {
                IceDisplayTab(
                    state = state,
                    blinkClockSeconds = { clock.value },
                    onTierChange = { state = state.copy(tier = it) },
                    onStealthChange = { state = state.copy(stealthEngaged = it) },
                    onPurge = { state = state.purged() },
                    onReconnect = { state = state.reconnected() },
                )
            },
            backbone = { IceBackboneTab(state) },
        )
    }
}

/**
 * The always-visible strip above the tabs: threat badge, score, and the trace-lock alert.
 *
 * @param state The shared matrix state.
 */
@Composable
private fun IceStatusStrip(state: IceMatrixState) {
    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CyberBadge(
                text = if (state.connected) state.activeIceClass.threatBadge else "LINE GROUNDED",
                variant = if (state.connected) iceBadgeVariant(state.activeIceClass) else CyberBadgeVariant.Outline,
            )
            Text(
                text = "OVERWATCH ${state.scorePercent.roundToInt()}%",
                style = CyberTheme.typography.terminal,
                color = if (state.traceLocked) CyberTheme.semantics.colors.danger else CyberTheme.colors.textPrimary,
            )
        }
        if (state.traceLocked) {
            Text(
                text = "TRACE COMPLETE // BLACK ICE LOCKED",
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                style = CyberTheme.typography.terminal,
                color = CyberTheme.semantics.colors.danger,
            )
        }
    }
}

/**
 * The Display tab: the user-facing console surface.
 *
 * @param state The shared matrix state.
 * @param blinkClockSeconds Supplies time for the Black ICE blink. It is read in the draw phase only.
 * @param onTierChange Called with the tier that the stepped slider snaps to.
 * @param onStealthChange Called with the new stealth rocker position.
 * @param onPurge Called when the operator confirms the trace purge.
 * @param onReconnect Called when the operator reconnects after a purge.
 */
@Composable
private fun IceDisplayTab(
    state: IceMatrixState,
    blinkClockSeconds: () -> Float,
    onTierChange: (PenetrationTier) -> Unit,
    onStealthChange: (Boolean) -> Unit,
    onPurge: () -> Unit,
    onReconnect: () -> Unit,
) {
    var purgeArmed by remember { mutableStateOf(false) }
    val tiers = PenetrationTier.entries
    // The thumb follows the finger freely. The tier changes at each half step.
    var sliderPosition by remember { mutableFloatStateOf(state.tier.ordinal.toFloat()) }
    LaunchedEffect(state.tier) {
        // Follow tier changes from outside the slider, for example a reconnect.
        if (sliderPosition.roundToInt() != state.tier.ordinal) sliderPosition = state.tier.ordinal.toFloat()
    }
    LaunchedEffect(sliderPosition) {
        // When the thumb stops moving, it snaps into the nearest detent.
        delay(DETENT_SNAP_DELAY_MILLIS)
        sliderPosition = sliderPosition.roundToInt().toFloat()
    }

    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12)) {
        ShowcaseSectionLabel("INTRUSION TOPOLOGY")
        IntrusionTopology(state)

        ShowcaseSectionLabel("PERIMETER ICE")
        IcePerimeterBar(state = state, blinkClockSeconds = blinkClockSeconds)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IceClass.entries.forEach { iceClass ->
                Text(
                    text = "${iceClass.name.uppercase()} ${iceBlockRange(iceClass)}",
                    style = CyberTheme.typography.terminal,
                    color = iceClassColor(iceClass),
                )
            }
        }

        ShowcaseReadoutRow(
            label = "DEPTH",
            value = "TIER ${state.tier.ordinal + 1} // ${state.tier.label}",
            valueColor = CyberTheme.colors.primary,
        )
        CyberSlider(
            value = sliderPosition,
            valueRange = 0f..tiers.lastIndex.toFloat(),
            modifier = Modifier.padding(vertical = CyberTheme.spacing.dp12),
            enabled = state.connected,
            tickCount = tiers.size,
            appendedA11y = "Penetration depth",
            onValueChange = { position ->
                sliderPosition = position
                onTierChange(tiers[position.roundToInt().coerceIn(tiers.indices)])
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PACKET STEALTH: ${if (state.stealthEngaged) "ON" else "OFF"}",
                style = CyberTheme.typography.terminal,
                color = if (state.stealthEngaged) CyberTheme.semantics.colors.success else CyberTheme.colors.textSecondary,
            )
            CyberSwitch(
                checked = state.stealthEngaged,
                enabled = state.connected,
                appendedA11y = "Packet stealth",
                onCheckedChange = onStealthChange,
            )
        }

        when {
            !state.connected -> CyberButton(
                modifier = Modifier.fillMaxWidth(),
                style = CyberButtonStyle.Outline,
                size = CyberButtonSize.Medium,
                onClick = onReconnect,
            ) { Text(text = "RECONNECT LINE", style = CyberTheme.typography.terminal) }

            purgeArmed -> Row(horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12)) {
                CyberButton(
                    modifier = Modifier.weight(1f),
                    style = CyberButtonStyle.Primary,
                    size = CyberButtonSize.Medium,
                    onClick = {
                        purgeArmed = false
                        onPurge()
                    },
                ) { Text(text = "PURGE NOW", style = CyberTheme.typography.terminal) }
                CyberButton(
                    modifier = Modifier.weight(1f),
                    style = CyberButtonStyle.Outline,
                    size = CyberButtonSize.Medium,
                    onClick = { purgeArmed = false },
                ) { Text(text = "CANCEL", style = CyberTheme.typography.terminal) }
            }

            // The first tap lifts the safety cover. The second tap purges. This prevents an accidental purge.
            else -> CyberButton(
                modifier = Modifier.fillMaxWidth(),
                style = CyberButtonStyle.Outline,
                size = CyberButtonSize.Medium,
                onClick = { purgeArmed = true },
            ) { Text(text = "LIFT PURGE COVER", style = CyberTheme.typography.terminal) }
        }
    }
}

/**
 * The amber vector screen: subnet nodes, the path to the present depth, the
 * hostile tracer, and the trace-lock reticle.
 *
 * The screen is decorative. The status strip and readouts carry the same
 * information as text, so it has no accessibility node.
 *
 * @param state The shared matrix state.
 */
@Composable
private fun IntrusionTopology(state: IceMatrixState) {
    val gridColor = CyberTheme.semantics.colors.warning
    val dimColor = CyberTheme.colors.surfaceTertiary
    val tracerColor = CyberTheme.semantics.colors.danger
    val screenColor = CyberTheme.colors.surfaceSecondary
    val strokeWidth = CyberPrimitives.BorderWidths.dp1
    val reachedNode = if (state.connected) state.tier.ordinal + 1 else 0
    val score = state.scorePercent
    val locked = state.traceLocked

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(ShowcaseDimens.HeroChamberHeight)
            .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
            .background(screenColor)
            .clearAndSetSemantics { }
            .drawBehind {
                val points = NODE_X.zip(NODE_Y) { x, y -> Offset(x * size.width, y * size.height) }
                val strokePx = strokeWidth.toPx()
                points.zipWithNext().forEachIndexed { hop, (from, to) ->
                    val reached = hop < reachedNode
                    drawLine(if (reached) gridColor else dimColor, from, to, strokeWidth = strokePx)
                }
                points.forEachIndexed { node, point ->
                    val color = if (node <= reachedNode) gridColor else dimColor
                    drawCircle(color, radius = NODE_RADIUS_FRACTION * size.height, center = point, style = Stroke(strokePx))
                }
                // The hostile tracer runs back from the deepest node toward the entry node as the score grows.
                if (score >= IceMatrixSpec.GREY_ICE_ACTIVATION_PERCENT) {
                    val progress = (score / IceMatrixSpec.MAX_SCORE_PERCENT).coerceIn(0f, 1f)
                    val start = points.last()
                    val end = points.first()
                    drawLine(tracerColor, start, start + (end - start) * progress, strokeWidth = strokePx)
                }
                if (locked) {
                    val center = points.first()
                    // Shrink the reticle when the node is near an edge, so the whole ring stays on screen.
                    val radius = minOf(
                        RETICLE_RADIUS_FRACTION * size.height,
                        center.x,
                        center.y,
                        size.width - center.x,
                        size.height - center.y,
                    )
                    drawCircle(tracerColor, radius = radius, center = center, style = Stroke(strokePx))
                    drawLine(tracerColor, center - Offset(radius, 0f), center + Offset(radius, 0f), strokePx)
                    drawLine(tracerColor, center - Offset(0f, radius), center + Offset(0f, radius), strokePx)
                }
            },
    )
}

/**
 * The 20-block perimeter ICE bar. Lit blocks take the color of their ICE class.
 * Above trace lock, the Black ICE blocks blink at [BLACK_ICE_BLINK_HZ].
 *
 * @param state The shared matrix state.
 * @param blinkClockSeconds Supplies time for the blink. It is read in the draw phase only.
 */
@Composable
private fun IcePerimeterBar(state: IceMatrixState, blinkClockSeconds: () -> Float) {
    val unlitColor = CyberTheme.colors.surfaceTertiary
    val classColors = IceClass.entries.associateWith { iceClassColor(it) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CyberTheme.spacing.dp16)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp2),
    ) {
        for (block in 1..IceMatrixSpec.ICE_BAR_BLOCKS) {
            val iceClass = iceClassOfBlock(block)
            val lit = block <= state.litBlocks
            val blinks = lit && state.traceLocked && iceClass == IceClass.Black
            val color = if (lit) classColors.getValue(iceClass) else unlitColor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(CyberTheme.spacing.dp16)
                    .drawBehind {
                        val darkHalf = blinks && (blinkClockSeconds() * BLACK_ICE_BLINK_HZ).toInt() % 2 == 1
                        drawRect(if (darkHalf) color.copy(alpha = BLINK_DIM_ALPHA) else color)
                    },
            )
        }
    }
}

/**
 * The Backbone tab: the hidden equipment behind the console.
 *
 * @param state The shared matrix state.
 */
@Composable
private fun IceBackboneTab(state: IceMatrixState) {
    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
        ShowcaseSectionLabel("SECURITY COPROCESSOR")
        Text(
            text = "SCORE = f(TIER) × (1 − STEALTH) × DWELL",
            style = CyberTheme.typography.terminal,
            color = CyberTheme.colors.textPrimary,
        )
        ShowcaseReadoutRow("f(TIER) [PLACEHOLDER]", "${state.tier.placeholderGrowthPercentPerSecond} %/s")
        ShowcaseReadoutRow("1 − STEALTH", TWO_DECIMALS.format(state.stealthFactor))
        ShowcaseReadoutRow("DWELL", "${ONE_DECIMAL.format(state.dwellSeconds)} s")
        ShowcaseReadoutRow(
            label = "OVERWATCH SCORE",
            value = "${ONE_DECIMAL.format(state.scorePercent)} %",
            valueColor = if (state.traceLocked) CyberTheme.semantics.colors.danger else CyberTheme.colors.primary,
        )

        ShowcaseSectionLabel("DETECTION BANDS (DOSSIER)")
        PenetrationTier.entries.forEach { tier ->
            val markers = buildList {
                if (state.connected && tier == state.tier) add("DEPTH")
                if (state.connected && tier == state.scoreBandTier) add("SCORE")
            }
            ShowcaseReadoutRow(
                label = "${tier.bandStartPercent.roundToInt()}–${tier.bandEndPercent.roundToInt()}% ${tier.label}",
                value = when (markers.size) {
                    0 -> "·"
                    1 -> "◀ ${markers.single()}"
                    else -> "◀ BOTH"
                },
                valueColor = CyberTheme.colors.primary,
            )
        }

        ShowcaseSectionLabel("STEALTH FILTER")
        val filterState = if (state.stealthEngaged && state.connected) "ACTIVE" else "BYPASSED"
        ShowcaseReadoutRow("HEADER SCRAMBLE", filterState)
        ShowcaseReadoutRow("MAC FORGERY", filterState)
        ShowcaseReadoutRow("PROXY ROUTING", filterState)
        ShowcaseReadoutRow("LATENCY PENALTY", if (filterState == "ACTIVE") "ADDED" else "NONE")

        ShowcaseSectionLabel("SUBSYSTEMS")
        ShowcaseReadoutRow("SIREN (MUTED)", sirenText(state.sirenState))
        ShowcaseReadoutRow("HAPTIC MOTORS", if (state.traceLocked) "ENGAGED" else "IDLE")
        ShowcaseReadoutRow("OPTICAL RELAYS", if (state.connected) "CLOSED" else "OPEN")
        ShowcaseReadoutRow(
            label = "DECRYPTION SHUNT",
            value = "${if (state.traceLocked) IceMatrixSpec.SHUNT_TRACE_LOCK_CELSIUS else IceMatrixSpec.SHUNT_NOMINAL_CELSIUS}°C",
        )
    }
}

/**
 * @param siren The siren state.
 * @return The siren state and its dossier frequency, as text.
 */
private fun sirenText(siren: IceSirenState): String = when (siren) {
    IceSirenState.Silent -> "OFF"
    IceSirenState.Chirp -> "CHIRP ${IceMatrixSpec.SIREN_CHIRP_HZ} Hz"
    IceSirenState.Warble -> "WARBLE ${IceMatrixSpec.SIREN_WARBLE_LOW_HZ}–${IceMatrixSpec.SIREN_WARBLE_HIGH_HZ} Hz"
    IceSirenState.Klaxon -> "KLAXON ${IceMatrixSpec.SIREN_KLAXON_HZ} Hz"
}

/**
 * @param iceClass An ICE class.
 * @return The one-based block range of that class, for example "1–8".
 */
private fun iceBlockRange(iceClass: IceClass): String = when (iceClass) {
    IceClass.White -> "1–${IceMatrixSpec.WHITE_ICE_LAST_BLOCK}"
    IceClass.Grey -> "${IceMatrixSpec.WHITE_ICE_LAST_BLOCK + 1}–${IceMatrixSpec.GREY_ICE_LAST_BLOCK}"
    IceClass.Black -> "${IceMatrixSpec.GREY_ICE_LAST_BLOCK + 1}–${IceMatrixSpec.ICE_BAR_BLOCKS}"
}

/**
 * Maps an ICE class to a semantic theme color by meaning: info for passive
 * monitors, warning for active barriers, danger for lethal feedback.
 *
 * @param iceClass An ICE class.
 * @return The theme color for that class.
 */
@Composable
private fun iceClassColor(iceClass: IceClass): Color = when (iceClass) {
    IceClass.White -> CyberTheme.semantics.colors.info
    IceClass.Grey -> CyberTheme.semantics.colors.warning
    IceClass.Black -> CyberTheme.semantics.colors.danger
}

/**
 * @param iceClass An ICE class.
 * @return The badge variant that matches the class color.
 */
private fun iceBadgeVariant(iceClass: IceClass): CyberBadgeVariant = when (iceClass) {
    IceClass.White -> CyberBadgeVariant.Info
    IceClass.Grey -> CyberBadgeVariant.Caution
    IceClass.Black -> CyberBadgeVariant.Danger
}
