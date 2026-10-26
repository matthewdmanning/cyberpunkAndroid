package com.example.cyberpunkandroid.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberComponentSemantics
import com.example.cyberpunkandroid.effects.cyberLeaderLine
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * One row of a [CyberDataList].
 *
 * @param key Field name, shown at the start of the row.
 * @param value Field value, shown at the end of the row.
 * @param valueColor Optional value color, for example a status role color for a status field.
 *   [Color.Unspecified] uses the list's value color.
 */
@Immutable
data class CyberDataEntry(
    val key: String,
    val value: String,
    val valueColor: Color = Color.Unspecified,
)

/**
 * Read-only list of labeled values, like a spec sheet or a system diagnostics panel.
 *
 * ### Visual behavior
 * - Each row shows the key at the start in [keyColor] and the value at the end in [valueColor]. A dotted leader
 *   ([cyberLeaderLine]) fills the space between them, so the eye can follow a row across a wide gap.
 * - Rows are separated by [rowSpacing]. There is no container, border, or motion, so the list fits inside a
 *   [CyberCard], a [CyberReadout] group, or a dialog.
 * - Long values wrap and stay end-aligned. Keys do not wrap. The leader sits on the baseline of the first line.
 *
 * ### Accessibility
 * Each row merges into one node, so screen readers read the key and value together.
 *
 * @param entries Rows, from top to bottom.
 * @param modifier Modifier applied to the list.
 * @param rowSpacing Vertical space between rows.
 * @param leaderStrokeWidth Dot diameter of the leader.
 * @param leaderGap Space between leader dots.
 * @param keyColor Key text color.
 * @param valueColor Default value text color.
 * @param leaderColor Leader dot color.
 * @param textStyle Text style of keys and values.
 * @param appendedA11y Optional accessibility label for the whole list. Without it, each row is read on its own.
 * @param customA11y Optional accessibility label that replaces [appendedA11y].
 */
@Composable
fun CyberDataList(
    entries: List<CyberDataEntry>,
    modifier: Modifier = Modifier,
    rowSpacing: Dp = CyberTheme.spacing.dp8,
    leaderStrokeWidth: Dp = CyberPrimitives.BorderWidths.dp2,
    leaderGap: Dp = CyberTheme.spacing.dp4,
    keyColor: Color = CyberTheme.colors.textSecondary,
    valueColor: Color = CyberTheme.colors.textPrimary,
    leaderColor: Color = CyberTheme.colors.border,
    textStyle: TextStyle = CyberTheme.typography.terminal,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    Column(
        modifier = modifier.cyberComponentSemantics("CyberDataList", appendedA11y, customA11y),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(CyberTheme.spacing.dp8)
            ) {
                Text(
                    text = entry.key.uppercase(),
                    color = keyColor,
                    style = textStyle,
                    softWrap = false,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(leaderStrokeWidth)
                        // Align the leader's bottom edge with the first-line baseline, so the dots sit on the text line.
                        .alignBy { placeable -> placeable.measuredHeight }
                        .cyberLeaderLine(color = leaderColor, strokeWidth = leaderStrokeWidth, gap = leaderGap)
                )
                Text(
                    text = entry.value,
                    color = entry.valueColor.takeOrElse { valueColor },
                    style = textStyle,
                    textAlign = TextAlign.End,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
    }
}
