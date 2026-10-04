package com.example.cyberpunkandroid.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import kotlin.math.sqrt
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * A honeycomb layout that arranges its children in an interlocking hexagonal grid.
 *
 * Unadorned base layout: purely calculates positional geometry for the nodes
 * and provides foundational semantics (CollectionInfo) without imposing visual flair.
 *
 * @param modifier Composable modifier for the grid container.
 * @param columns Number of nodes per row.
 * @param hexRadius The circumradius of a single hexagonal node (center to vertex).
 */
@Composable
fun CyberHexGrid(
    modifier: Modifier = Modifier,
    columns: Int = 3,
    hexRadius: Float = 100f,
    appendedA11y: String? = null,
    customA11y: String? = null,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier.cyberComponentSemantics("CyberHexGrid", appendedA11y, customA11y).semantics {
            collectionInfo = CollectionInfo(rowCount = -1, columnCount = columns)
        }
    ) { measurables, constraints ->
        // Hexagon math (pointy-topped)
        val hexWidth = sqrt(3f) * hexRadius
        val hexHeight = 2f * hexRadius
        val horizontalSpacing = hexWidth
        val verticalSpacing = hexHeight * 0.75f

        val placeables = measurables.map { measurable ->
            // Constrain children to the size of the hexagon
            measurable.measure(
                Constraints.fixed(
                    width = hexWidth.toInt(),
                    height = hexHeight.toInt()
                )
            )
        }

        // Calculate layout size bounds
        val rows = Math.ceil(placeables.size.toDouble() / columns).toInt()
        val totalWidth = if (placeables.isEmpty()) 0 else (columns * horizontalSpacing + (horizontalSpacing / 2f)).toInt()
        val totalHeight = if (placeables.isEmpty()) 0 else ((rows * verticalSpacing) + (hexHeight * 0.25f)).toInt()

        layout(
            width = totalWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        ) {
            placeables.forEachIndexed { index, placeable ->
                val col = index % columns
                val row = index / columns

                // Offset odd rows to create the honeycomb interlocking effect
                val xOffset = if (row % 2 == 1) horizontalSpacing / 2f else 0f
                val x = (col * horizontalSpacing + xOffset).toInt()
                val y = (row * verticalSpacing).toInt()

                placeable.placeRelative(x = x, y = y)
            }
        }
    }
}
