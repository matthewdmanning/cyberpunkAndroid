package com.example.sample.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.example.cyberpunkandroid.components.CyberBadge
import com.example.cyberpunkandroid.components.CyberBadgeVariant
import com.example.cyberpunkandroid.components.CyberCard
import com.example.cyberpunkandroid.components.CyberProgress
import com.example.cyberpunkandroid.components.CyberProgressVariant
import com.example.cyberpunkandroid.theme.CyberTheme
import kotlin.math.roundToInt

/** Design: number of LED segments in the booster current bar. */
private const val CURRENT_BAR_SEGMENTS = 16

/** Design: dimmest alpha of a lit light-pipe, so dark pipes still read as glass. */
private const val PIPE_MIN_ALPHA = 0.12f

/** Factor that turns a 0–1 fraction into a percent. */
private const val PERCENT = 100

/**
 * Roadmap card 6, Level 1: the Militech MBD-1553B optical hardware bus diagnostic unit.
 *
 * The card shows the "Combat Load Drift" state of the dossier
 * (`docs/inspirations/mil-spec-hardware-bus-diagnostic.md` §4.2) as a passive monitor:
 *
 * - **Optical bus light-pipes:** 32 columns, one per compute thread. A bright
 *   packet drifts across the bank to where the load is. Text shows the load
 *   centroid and the count of active threads, so the state never depends on brightness alone.
 * - **Firewall barrier:** a steady bar at full integrity, with the integrity as text.
 * - **Power rail flux:** a segmented bar that kicks on each booster current spike
 *   and decays, with the current in amperes as text.
 *
 * The card has no controls. TODO(controls): the dossier §3 mode selector, guarded
 * isolation toggle, and calibration knob, with the breach and sever states they drive.
 * TODO(audio): relay clicks and the 120 Hz chassis thrum (dossier §3).
 *
 * When the system "Remove animations" setting is on, the card shows one static frame.
 *
 * @param modifier Modifier applied to the card.
 */
@Composable
fun BusDiagnosticCard(modifier: Modifier = Modifier) {
    val motionEnabled = rememberSystemMotionEnabled()
    val clock = rememberShowcaseClockSeconds(running = motionEnabled)
    // Recompose the text only when a shown integer changes, not on every frame.
    val centroidThread by remember { derivedStateOf { busDiagnosticFrameAt(clock.value).centroidThread } }
    val activeThreads by remember { derivedStateOf { busDiagnosticFrameAt(clock.value).activeThreads } }
    val currentAmps by remember { derivedStateOf { busDiagnosticFrameAt(clock.value).railCurrentAmps.roundToInt() } }
    val firewallIntegrity by remember { derivedStateOf { busDiagnosticFrameAt(clock.value).firewallIntegrity } }

    CyberCard(
        modifier = modifier.fillMaxWidth(),
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "MIL-SPEC BUS DIAGNOSTIC",
                    style = CyberTheme.typography.terminal,
                    color = CyberTheme.colors.primary,
                )
                CyberBadge(text = "ACTIVE LOAD", variant = CyberBadgeVariant.Info)
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CyberTheme.spacing.dp16),
            verticalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp12),
        ) {
            ShowcaseSectionLabel("OPTICAL BUS // ${BusDiagnosticSpec.LIGHT_PIPE_COUNT} THREADS")
            LightPipeBank(clockSeconds = { clock.value })
            ShowcaseReadoutRow(label = "LOAD CENTROID", value = "THREAD ${centroidThread + 1}")
            ShowcaseReadoutRow(
                label = "ACTIVE THREADS",
                value = "$activeThreads / ${BusDiagnosticSpec.LIGHT_PIPE_COUNT}",
            )

            ShowcaseSectionLabel("FIREWALL BARRIER")
            CyberProgress(
                progress = firewallIntegrity,
                color = CyberTheme.semantics.colors.info,
                customA11y = "Firewall barrier integrity",
            )
            ShowcaseReadoutRow(
                label = "INTEGRITY",
                value = "${(firewallIntegrity * PERCENT).roundToInt()}% // STEADY",
                valueColor = CyberTheme.semantics.colors.info,
            )

            ShowcaseSectionLabel("POWER RAIL FLUX")
            CyberProgress(
                progress = currentAmps / BusDiagnosticSpec.MAX_SPIKE_CURRENT_AMPS,
                variant = CyberProgressVariant.Segmented,
                segments = CURRENT_BAR_SEGMENTS,
                color = CyberTheme.semantics.colors.success,
                customA11y = "Booster current",
            )
            ShowcaseReadoutRow(
                label = "BOOSTER CURRENT",
                value = "${currentAmps}A / ${BusDiagnosticSpec.MAX_SPIKE_CURRENT_AMPS.roundToInt()}A MAX",
            )
            BusDiagnosticSpec.RAIL_LABELS.forEach { rail ->
                ShowcaseReadoutRow(
                    label = rail,
                    value = if (rail == BusDiagnosticSpec.NEEDLE_RAIL_LABEL) {
                        "${BusDiagnosticSpec.NOMINAL_RAIL_VOLTS}V IN TOLERANCE"
                    } else {
                        "IN TOLERANCE"
                    },
                    valueColor = CyberTheme.semantics.colors.success,
                )
            }
        }
    }
}

/**
 * The bank of vertical light-pipes, drawn in the draw phase only.
 *
 * The clock is read inside [drawBehind], so each frame redraws the bank without
 * a recomposition. The bank is decorative: the readout rows below it carry the
 * same information as text, so it has no accessibility node.
 *
 * @param clockSeconds Supplies the simulation time for the frame that is drawn.
 */
@Composable
private fun LightPipeBank(clockSeconds: () -> Float) {
    val litColor = CyberTheme.semantics.colors.success
    val glassColor = CyberTheme.colors.surfaceSecondary
    val pipeGap = CyberTheme.spacing.dp2
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(ShowcaseDimens.PipeBankHeight)
            .clip(CyberTheme.shapes.roundedSm)
            .clearAndSetSemantics { }
            .drawBehind {
                val frame = busDiagnosticFrameAt(clockSeconds())
                val gapPx = pipeGap.toPx()
                val pipeCount = frame.pipeIntensities.size
                val pipeWidth = (size.width - gapPx * (pipeCount - 1)) / pipeCount
                frame.pipeIntensities.forEachIndexed { index, intensity ->
                    val left = index * (pipeWidth + gapPx)
                    drawRect(color = glassColor, topLeft = Offset(left, 0f), size = Size(pipeWidth, size.height))
                    drawRect(
                        color = litColor.copy(alpha = PIPE_MIN_ALPHA + (1f - PIPE_MIN_ALPHA) * intensity),
                        topLeft = Offset(left, 0f),
                        size = Size(pipeWidth, size.height),
                    )
                }
            },
    )
}
