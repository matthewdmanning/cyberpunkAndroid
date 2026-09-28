package com.example.cyberpunkandroid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.components.*
import com.example.cyberpunkandroid.effects.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"], sdk = [33])
class CyberComponentTestSuite(
    private val componentName: String,
    private val componentFactory: @Composable () -> Unit,
    private val modifierName: String,
    private val modifierFactory: @Composable Modifier.() -> Modifier
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testComponentRendering() {
        composeTestRule.setContent {
            Box(modifier = Modifier.padding(16.dp).modifierFactory()) {
                componentFactory()
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0} with {2}")
        fun data(): Collection<Array<Any>> {
            val components = listOf<Pair<String, @Composable () -> Unit>>(
                "CyberAlert" to { CyberAlert(title = "ALERT", message = "Test message") },
                "CyberBadge" to { CyberBadge(text = "BADGE") },
                "CyberButton" to { CyberButton(onClick = {}) { Text("BUTTON") } },
                "CyberCard" to { CyberCard { Text("Card Content") } },
                "CyberProgress" to { CyberProgress(progress = 0.5f) },
                "CyberSkeleton" to { CyberSkeleton() },
                "CyberSpinner" to { CyberSpinner() },
                "CyberDropdown" to { CyberDropdown(items = listOf("A", "B"), selectedIndex = 0, onItemSelected = {}) },
                "CyberModal" to { CyberModal(onDismissRequest = {}, title = "MODAL") { Text("Content") } },
                "CyberTable" to { CyberTable(headers = listOf("H1"), rows = listOf(listOf("R1"))) },
                "CyberTabs" to { CyberTabs(tabs = listOf("T1", "T2"), selectedTabIndex = 0, onTabSelected = {}) },
                "CyberTerminal" to { CyberTerminal(title = "terminal") { Text("Terminal output") } },
                "CyberCheckbox" to { CyberCheckbox(checked = true, onCheckedChange = {}) },
                "CyberField" to { CyberField(label = "FIELD") { isError -> CyberTextField(value = "", onValueChange = {}, isError = isError) } },
                "CyberTextField" to { CyberTextField(value = "Text", onValueChange = {}) },
                "CyberTextArea" to { CyberTextArea(value = "Area", onValueChange = {}) },
                "CyberNavigationBar" to { CyberNavigationBar(brand = "NAV", items = listOf("Home"), selectedIndex = 0, onItemSelected = {}) },
                "CyberSpinnerOverlay" to { CyberSpinnerOverlay(text = "LOADING") }
            )

            val modifiers = listOf<Pair<String, @Composable Modifier.() -> Modifier>>(
                "None" to { this },
                "cyberDatastream" to { cyberDatastream(color = Color.Cyan) },
                "cyberOverload" to { cyberOverload() },
                "cyberNeonBorder" to { cyberNeonBorder(color = Color.Cyan, shape = RoundedCornerShape(4.dp)) },
                "cyberScanlines" to { cyberScanlines() },
                "cyberTextGlow" to { cyberTextGlow(color = Color.Cyan) }
            )

            val testCases = mutableListOf<Array<Any>>()

            // 1. Single Modifiers applied to each component
            for (component in components) {
                for (modifier in modifiers) {
                    testCases.add(arrayOf(component.first, component.second, modifier.first, modifier.second))
                }
            }

            // 2. Pairwise Modifiers applied to each component
            for (component in components) {
                for (i in modifiers.indices) {
                    for (j in i + 1 until modifiers.size) {
                        if (modifiers[i].first == "None" || modifiers[j].first == "None") continue
                        val mod1 = modifiers[i]
                        val mod2 = modifiers[j]
                        val combinedName = "${mod1.first} + ${mod2.first}"
                        val mod1Func = mod1.second
                        val mod2Func = mod2.second
                        val combinedModifier: @Composable Modifier.() -> Modifier = {
                            this.mod1Func().mod2Func()
                        }
                        testCases.add(arrayOf(component.first, component.second, combinedName, combinedModifier))
                    }
                }
            }

            return testCases
        }
    }
}
