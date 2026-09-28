package com.example.cyberpunkandroid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.cyberpunkandroid.config.CyberPrimitives

import com.example.cyberpunkandroid.effects.cyberBorder
import com.example.cyberpunkandroid.effects.cyberScanlines
import com.example.cyberpunkandroid.effects.cyberSemantics
import com.example.cyberpunkandroid.icons.CyberIcon
import com.example.cyberpunkandroid.icons.SemanticIcons
import com.example.cyberpunkandroid.theme.CyberTheme

/**
 * Visual intent variants for [CyberAlert].
 */
enum class CyberAlertVariant {
    /** Cyan informational notice. */
    Info,
    /** Green operation confirmation notice. */
    Success,
    /** Yellow cautionary warning banner. */
    Warning,
    /** Red critical error alert banner. */
    Error
}

/**
 * High-visibility sci-fi alert callout box featuring neon borders, semantic iconography, and cut-corner styling.
 *
 * Implements the Cybercore CSS `.cyber-alert` spec with dual-layer neon borders and integrated SVG icons.
 *
 * @param title Uppercase title string displayed prominently in display typography.
 * @param message Descriptive body copy explaining the alert context.
 * @param modifier Modifier applied to the outer alert container.
 * @param variant Visual priority and semantic intent variant ([CyberAlertVariant.Info], [CyberAlertVariant.Success],
 *   [CyberAlertVariant.Warning], [CyberAlertVariant.Error]). Defaults to [CyberAlertVariant.Info].
 * @param appendedA11y Optional text to append to the default component accessibility name.
 * @param customA11y Optional custom accessibility description that completely overrides the default name.
 */
@Composable
fun CyberAlert(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    variant: CyberAlertVariant = CyberAlertVariant.Info,
    iconSize: androidx.compose.ui.unit.Dp = CyberPrimitives.IconSizes.dp24,
    appendedA11y: String? = null,
    customA11y: String? = null,
) {
    val color = when (variant) {
        CyberAlertVariant.Info -> CyberTheme.semantics.colors.info
        CyberAlertVariant.Success -> CyberTheme.semantics.colors.success
        CyberAlertVariant.Warning -> CyberTheme.semantics.colors.warning
        CyberAlertVariant.Error -> CyberTheme.semantics.colors.error
    }

    val iconRes = when (variant) {
        CyberAlertVariant.Info -> SemanticIcons.Info
        CyberAlertVariant.Success -> SemanticIcons.Success
        CyberAlertVariant.Warning -> SemanticIcons.Caution
        CyberAlertVariant.Error -> SemanticIcons.Danger
    }

    Box(
        modifier = modifier
            .cyberSemantics("CyberAlert", appendedA11y, customA11y)
            .fillMaxWidth()
            .clip(CyberTheme.shapes.cyberCutCornerShape)
            .background(CyberTheme.colors.surface)
            .cyberBorder(color = color,
                width = CyberPrimitives.BorderWidths.dp2, shape = CyberTheme.shapes.cyberCutCornerShape)
            .padding(CyberPrimitives.Spacing.dp16)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            CyberIcon(
                iconRes = iconRes,
                contentDescription = variant.name,
                tint = color,
                size = iconSize
            )
            Spacer(modifier = Modifier.width(CyberPrimitives.Spacing.dp16))
            Column(
                verticalArrangement = Arrangement.spacedBy(CyberPrimitives.Spacing.dp4)
            ) {
                CompositionLocalProvider(LocalContentColor provides color) {
                    ProvideTextStyle(value = CyberTheme.typography.terminal) {
                        Text(text = title.uppercase())
                    }
                }
                CompositionLocalProvider(LocalContentColor provides CyberTheme.colors.textSecondary) {
                    ProvideTextStyle(value = CyberTheme.typography.body) {
                        Text(text = message)
                    }
                }
            }
        }
    }
}
