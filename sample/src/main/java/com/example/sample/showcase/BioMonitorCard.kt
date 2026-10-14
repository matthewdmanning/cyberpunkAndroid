package com.example.sample.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberBiometrics
import com.example.cyberpunkandroid.components.CyberButton
import com.example.cyberpunkandroid.components.CyberButtonSize
import com.example.cyberpunkandroid.components.CyberButtonStyle
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.cyberLongPressFill
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberCrt
import com.example.cyberpunkandroid.effects.cyberOverload
import com.example.cyberpunkandroid.effects.cyberSpark
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.CyberIcons
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/**
 * Design: hold time for the cold-jack lever, in milliseconds.
 * A touch screen cannot sense the dossier's 18 N pull force, so a hold stands in for it.
 */
private const val COLD_JACK_HOLD_MILLIS = 1_500L

/** Design: number of bezel corona sparks during fibrillation. */
private const val CORONA_SPARK_COUNT = 24

/** Factor that turns a 0–1 fraction into a percent. */
private const val PERCENT = 100

/**
 * Roadmap card 8, Level 4: the Trauma Team TT-CRT7 dumpshock cold-jack console.
 *
 * Source: `docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md`. The card has two tabs:
 *
 * - **Display:** the curved CRT (`cyberCrt`) with the EKG and synaptic trace,
 *   the bezel vital readouts, the bezel corona sparks (`cyberSpark`) during
 *   fibrillation, and the cold-jack lever. While the operator holds the lever,
 *   the CRT shader switches off and the dumpshock glitch (`cyberOverload`)
 *   switches on. Only one distortion shader runs at a time (cookbook rule 7).
 * - **Backbone:** the signal-source simulator and the hidden equipment: the
 *   interlink tap, the cardiac-neural coupling, the knife switch, the capacitor
 *   bank, the arc chute, and the flyback.
 *
 * TODO(controls): the gain and sweep knobs and the defibrillator charge rocker (dossier §3).
 * TODO(audio): the flyback whine and the R-wave "pock" sounder (dossier §3).
 *
 * @param modifier Modifier applied to the card.
 */
@Composable
fun BioMonitorCard(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(BioMonitorState()) }
    var leverProgress by remember { mutableFloatStateOf(0f) }
    val motionEnabled = rememberSystemMotionEnabled()
    val clock = rememberShowcaseClockSeconds(running = motionEnabled)

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Text(
                text = "CRT BIOMONITOR // COLD-JACK",
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.primary,
            )
        },
    ) {
        ShowcaseTabbedPanel(
            modifier = Modifier.padding(CyberTheme.spacing.dp16),
            statusStrip = { BioStatusStrip(state) },
            display = {
                BioDisplayTab(
                    state = state,
                    clockSeconds = { clock.value },
                    leverProgress = leverProgress,
                    onLeverProgress = { leverProgress = it },
                    onLeverComplete = {
                        state = if (state.coldJacked) state.reJack() else state.coldJack()
                        leverProgress = 0f
                    },
                )
            },
            backbone = {
                BioBackboneTab(state = state, onThreatChange = { state = state.withThreat(it) })
            },
        )
    }
}

/**
 * The always-visible strip above the tabs. It announces stage changes to screen readers.
 *
 * @param state The shared monitor state.
 */
@Composable
private fun BioStatusStrip(state: BioMonitorState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CyberBadge(text = state.shownStage.label, variant = bioBadgeVariant(state.shownStage))
        Text(
            text = if (state.coldJacked) "LINK SEVERED" else "LINK LIVE",
            style = CyberTheme.typography.terminal,
            color = CyberTheme.colors.textSecondary,
        )
    }
}

/**
 * The Display tab: the CRT, the bezel readouts, and the cold-jack lever.
 *
 * @param state The shared monitor state.
 * @param clockSeconds Supplies the simulation time at the right edge of the trace.
 * @param leverProgress Hold progress of the lever, from 0 to 1.
 * @param onLeverProgress Called with each new hold progress value.
 * @param onLeverComplete Called when the hold completes. It toggles between cold-jack and re-jack.
 */
@Composable
private fun BioDisplayTab(
    state: BioMonitorState,
    clockSeconds: () -> Float,
    leverProgress: Float,
    onLeverProgress: (Float) -> Unit,
    onLeverComplete: () -> Unit,
) {
    val stage = state.shownStage
    val leverHeld = leverProgress > 0f
    val phosphor = CyberTheme.semantics.colors.success

    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12)) {
        // One accessibility node for the whole monitor. It also hides the effect modifiers' own labels.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShowcaseDimens.CrtChamberHeight)
                .clearAndSetSemantics {
                    contentDescription = "Neural EKG monitor"
                    stateDescription = stage.label
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CyberTheme.shapes.cyberCutCornerShape)
                    // Macro-shaders sample the content inside them, so the background comes after them.
                    .cyberCrt(enabled = !leverHeld)
                    .cyberOverload(enabled = leverHeld)
                    .background(CyberTheme.colors.background)
                    .padding(CyberTheme.spacing.dp12),
            ) {
                CrtTrace(stage = stage, clockSeconds = clockSeconds, color = phosphor)
            }
            if (state.sparksActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .cyberSpark(
                            color = CyberTheme.colors.primary,
                            secondaryColor = CyberTheme.colors.textSecondary,
                            warningColor = CyberTheme.semantics.colors.warning,
                            sparkCount = CORONA_SPARK_COUNT,
                        ),
                )
            }
        }

        ShowcaseReadoutRow(
            label = "VENTRICULAR RATE (BPM)",
            value = stage.heartRateText,
            valueColor = bioStageColor(stage),
        )
        ShowcaseReadoutRow(
            label = "COGNITIVE COHERENCE",
            value = stage.coherenceText,
            valueColor = bioStageColor(stage),
        )

        ColdJackLever(
            coldJacked = state.coldJacked,
            progress = leverProgress,
            onProgress = onLeverProgress,
            onComplete = onLeverComplete,
        )
    }
}

/**
 * The phosphor trace inside the CRT.
 *
 * The clock is read here, so only this small scope recomposes on each frame.
 *
 * @param stage The stage whose signal the trace shows.
 * @param clockSeconds Supplies the simulation time at the right edge of the trace.
 * @param color The phosphor color of the trace line.
 */
@Composable
private fun CrtTrace(stage: BioStage, clockSeconds: () -> Float, color: Color) {
    CyberBiometrics(
        data = bioTraceAt(stage, clockSeconds()),
        modifier = Modifier.fillMaxSize(),
        color = color,
    )
}

/**
 * The red cold-jack lever. The operator holds it for [COLD_JACK_HOLD_MILLIS] to pull it.
 *
 * A screen reader user activates it with the long-press action. Thus the lever
 * does not depend on a drag or a timed hold.
 *
 * @param coldJacked `true` when the link is severed. The next hold re-jacks the link.
 * @param progress Hold progress, from 0 to 1. The lever track fills downward by this amount.
 * @param onProgress Called with each new hold progress value.
 * @param onComplete Called when the hold completes.
 */
@Composable
private fun ColdJackLever(
    coldJacked: Boolean,
    progress: Float,
    onProgress: (Float) -> Unit,
    onComplete: () -> Unit,
) {
    val actionLabel = if (coldJacked) "Re-jack neural link" else "Sever neural link"
    Row(
        horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(ShowcaseDimens.LeverWidth)
                .height(ShowcaseDimens.LeverTrackHeight)
                .clip(CyberTheme.shapes.cyberCutCornerShapeSmall)
                .background(CyberTheme.colors.surfaceTertiary)
                .semantics {
                    role = Role.Button
                    contentDescription = "Cold-jack lever"
                    stateDescription = if (coldJacked) "Link severed" else "Link connected"
                    onLongClick(label = actionLabel) {
                        onComplete()
                        true
                    }
                }
                .cyberLongPressFill(
                    durationMillis = COLD_JACK_HOLD_MILLIS,
                    onProgressUpdate = onProgress,
                    onComplete = onComplete,
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress)
                    .background(CyberTheme.semantics.colors.danger),
            )
            CyberIcon(
                iconRes = if (coldJacked) CyberIcons.Check else CyberIcons.Unlink,
                contentDescription = null,
                size = CyberPrimitives.IconSizes.dp24,
                tint = if (coldJacked) CyberTheme.semantics.colors.success else CyberTheme.semantics.colors.danger,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp4)) {
            Text(
                text = when {
                    progress > 0f -> "PULLING: ${(progress * PERCENT).roundToInt()}%"
                    coldJacked -> "KNIFE SWITCH OPEN"
                    else -> "COLD-JACK LEVER"
                },
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.textPrimary,
            )
            Text(
                text = if (coldJacked) "HOLD TO RE-JACK" else "HOLD TO SEVER LINK",
                style = CyberTheme.typography.terminal,
                color = CyberTheme.colors.textSecondary,
            )
        }
    }
}

/**
 * The Backbone tab: the signal-source simulator and the hidden equipment.
 *
 * @param state The shared monitor state.
 * @param onThreatChange Called with the stage that the operator selects for the signal source.
 */
@Composable
private fun BioBackboneTab(state: BioMonitorState, onThreatChange: (BioStage) -> Unit) {
    val linked = !state.coldJacked
    val threatStages = listOf(BioStage.Patrol, BioStage.Stress, BioStage.Fibrillation)
    Column(verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
        ShowcaseSectionLabel("SIGNAL SOURCE // SIMULATION")
        Row(horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)) {
            threatStages.forEach { stage ->
                CyberButton(
                    modifier = Modifier.weight(1f),
                    style = if (state.threatStage == stage) CyberButtonStyle.Primary else CyberButtonStyle.Outline,
                    size = CyberButtonSize.Small,
                    onClick = { onThreatChange(stage) },
                ) { Text(text = threatButtonText(stage), style = CyberTheme.typography.terminal) }
            }
        }

        ShowcaseSectionLabel("NEURAL INTERLINK TAP")
        ShowcaseReadoutRow(
            label = "DIFF AMP RANGE",
            value = "${BioMonitorSpec.TAP_MIN_MICROVOLTS}–${BioMonitorSpec.TAP_MAX_MICROVOLTS.roundToInt()} µV",
        )
        ShowcaseReadoutRow("UMBILICAL", if (linked) "SIGNAL" else "NO SIGNAL")

        ShowcaseSectionLabel("CARDIAC-NEURAL COUPLING")
        ShowcaseReadoutRow("SOURCE HEART RATE", "${state.threatStage.heartRateText} BPM")
        ShowcaseReadoutRow("SOURCE COHERENCE", state.threatStage.coherenceText)

        ShowcaseSectionLabel("DUMPSHOCK SHUNT")
        ShowcaseReadoutRow(
            label = "KNIFE SWITCH",
            value = if (linked) "CLOSED" else "OPEN",
            valueColor = if (linked) CyberTheme.colors.textPrimary else CyberTheme.semantics.colors.success,
        )
        ShowcaseReadoutRow(
            label = "CAPACITOR BANK",
            value = if (linked) {
                "ARMED // ${BioMonitorSpec.CAPACITOR_ABSORB_KILOVOLTS} kV IN ${BioMonitorSpec.CAPACITOR_ABSORB_MICROSECONDS} µs"
            } else {
                "FEEDBACK DUMPED"
            },
        )
        ShowcaseReadoutRow("ARC CHUTE", if (linked) "IDLE" else "ARC FLASH LOGGED")

        ShowcaseSectionLabel("CRT DRIVE")
        ShowcaseReadoutRow("FLYBACK (SILENT)", "${BioMonitorSpec.FLYBACK_KILOHERTZ} kHz")
        ShowcaseReadoutRow("R-WAVE SOUNDER (SILENT)", if (linked) "PULSES PER BEAT" else "OFF")
    }
}

/**
 * @param stage A threat stage that the signal source can select.
 * @return The short button text for that stage.
 */
private fun threatButtonText(stage: BioStage): String = when (stage) {
    BioStage.Patrol -> "PATROL"
    BioStage.Stress -> "HOSTILE"
    BioStage.Fibrillation -> "BLACK ICE"
    BioStage.Stabilized -> "ISOLATED"
}

/**
 * @param stage A biometric stage.
 * @return The badge variant for that stage.
 */
private fun bioBadgeVariant(stage: BioStage): CyberBadgeVariant = when (stage) {
    BioStage.Patrol -> CyberBadgeVariant.Success
    BioStage.Stress -> CyberBadgeVariant.Caution
    BioStage.Fibrillation -> CyberBadgeVariant.Danger
    BioStage.Stabilized -> CyberBadgeVariant.Info
}

/**
 * @param stage A biometric stage.
 * @return The semantic theme color for that stage's readouts.
 */
@Composable
private fun bioStageColor(stage: BioStage) = when (stage) {
    BioStage.Patrol -> CyberTheme.semantics.colors.success
    BioStage.Stress -> CyberTheme.semantics.colors.warning
    BioStage.Fibrillation -> CyberTheme.semantics.colors.danger
    BioStage.Stabilized -> CyberTheme.semantics.colors.info
}
