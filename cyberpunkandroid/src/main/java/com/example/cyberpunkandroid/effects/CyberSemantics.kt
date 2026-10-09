package com.example.cyberpunkandroid.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Standardized accessibility modifier for Cyberpunk UI elements.
 * Allows composition of default component/modifier names with appended context,
 * or completely overriding the accessibility description.
 *
 * @param name The default name of the component or effect (e.g., "CyberOverload", "CyberSpinner").
 * @param appendedA11y Optional text to append to the default name for extra context.
 * @param customA11y Optional completely custom text that overrides the default name and appended text.
 */
fun Modifier.cyberSemantics(
    name: String,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.semantics(mergeDescendants = true) {
    contentDescription = customA11y ?: buildString {
        append(name)
        if (!appendedA11y.isNullOrBlank()) {
            append(" - ")
            append(appendedA11y)
        }
    }
}

/**
 * Component variant of [cyberSemantics]: applies the label only when the caller supplies [appendedA11y] or
 * [customA11y]. Without them, screen readers keep announcing the component's own content (a button's text,
 * a table's cells) instead of its type name. When supplied, developer labels are used directly without code name prefixes.
 *
 * @param name Unused component name retained for binary/source compatibility.
 * @param appendedA11y Optional text for extra context.
 * @param customA11y Optional text that replaces the whole label.
 */
fun Modifier.cyberComponentSemantics(
    name: String,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier {
    val description = customA11y ?: appendedA11y
    return if (description.isNullOrBlank()) this
    else this.semantics(mergeDescendants = true) {
        contentDescription = description
    }
}
