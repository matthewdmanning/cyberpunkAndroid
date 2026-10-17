package com.example.sample.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Container sizes for showcase cards 6 to 8.
 *
 * Each value comes from `docs/SHOWCASE_ROADMAP.md` §2 unless its comment says "design".
 */
object ShowcaseDimens {
    /** Roadmap §2A: hero chambers that host shaders or vector screens are at least 140 dp tall. */
    val HeroChamberHeight = 140.dp

    /** Roadmap §2A: upper hero height. It also gives the 60 dp spark throw clearance. */
    val CrtChamberHeight = 180.dp

    /** Roadmap §2A: terminal buffers are at least 72 dp tall. Used for the light-pipe bank. */
    val PipeBankHeight = 72.dp

    /** Roadmap §2B: actuator dials are at most 108 dp. Used for the lever track length. */
    val LeverTrackHeight = 108.dp

    /** Roadmap §2B: inner capacitive actuators are at least 56 dp wide. */
    val LeverWidth = 56.dp

    /** Design: square area at each bezel corner that emits corona sparks. */
    val CoronaEmitterSize = 48.dp
}

/**
 * A terminal-style section heading inside a showcase card.
 *
 * The text is marked as a heading, so screen reader users can jump between sections.
 *
 * @param text The heading text.
 * @param modifier Modifier applied to the text.
 */
@Composable
fun ShowcaseSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = CyberTheme.typography.terminal,
        color = CyberTheme.colors.textSecondary,
    )
}

/**
 * One label and value pair, with the label at the start and the value at the end.
 *
 * The row merges into one accessibility node, so a screen reader reads "label, value" together.
 * The label takes the remaining width and wraps first, so the value stays on one line.
 *
 * @param label What the value measures.
 * @param value The present value as text. Text carries the state, so state never depends on color alone.
 * @param modifier Modifier applied to the row.
 * @param valueColor Color of the value text. Defaults to the primary text color.
 */
@Composable
fun ShowcaseReadoutRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = CyberTheme.colors.textPrimary,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = CyberTheme.spacing.dp8),
            style = CyberTheme.typography.terminal,
            color = CyberTheme.colors.textSecondary,
        )
        Text(text = value, style = CyberTheme.typography.terminal, color = valueColor, maxLines = 1)
    }
}
