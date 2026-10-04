package com.example.cyberpunkandroid.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.cyberpunkandroid.config.CyberPrimitives
import com.example.cyberpunkandroid.effects.cyberOverload
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cyberpunkandroid.effects.cyberComponentSemantics

/**
 * Structured configuration for a feedback test.
 */
data class FeedbackConfig(
    val testName: String,
    val iconSizeDp: Int,
    val values: List<Float>
)

/**
 * Loads a FeedbackConfig from a JSON file in the assets folder.
  * @param fileName TODO: document this
 */
fun loadFeedbackConfigFromAssets(context: Context, fileName: String): FeedbackConfig? {
    return try {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        val valuesArray = json.getJSONArray("values")
        val values = List(valuesArray.length()) { i -> valuesArray.getDouble(i).toFloat() }

        FeedbackConfig(
            testName = json.getString("test_name"),
            iconSizeDp = json.getInt("icon_size_dp"),
            values = values
        )
    } catch (e: Exception) {
        Log.e("CyberFeedback", "Failed to load config from $fileName", e)
        null
    }
}

// TODO: document this
fun resetFeedbackFiles(context: Context) {
    try {
        val ratingsFile = File(context.filesDir, "ratings.json")
        if (ratingsFile.exists()) {
            val timestamp = System.currentTimeMillis()
            val archiveFile = File(context.filesDir, "ratings_archive_$timestamp.json")
            ratingsFile.copyTo(archiveFile, overwrite = true)
        }

        val files = context.filesDir.listFiles()
        files?.forEach { file ->
            if (file.name.startsWith("feedback_fixture_") && file.name.endsWith(".json")) {
                file.delete()
            }
        }
        if (ratingsFile.exists()) {
            ratingsFile.delete()
        }
        Log.d("CyberFeedback", "Archived and cleared all feedback JSON files.")
    } catch (e: Exception) {
        Log.e("CyberFeedback", "Failed to clear feedback files", e)
    }
}

/**
 * A testing fixture to gather physical device feedback on parameter values.
 * Displays an adaptive grid of items based on the provided icon size, allowing the user to tap to grade them (+1, -1, 0).
 * Results are automatically recorded to local JSON on device storage.
  * @param modifier TODO: document this
  * @param resetKey TODO: document this
  * @param appendedA11y TODO: document this
  * @param customA11y TODO: document this
 */
@Composable
fun CyberFeedbackFixture(
    config: FeedbackConfig,
    modifier: Modifier = Modifier,
    resetKey: Int = 0,
    appendedA11y: String? = null,
    customA11y: String? = null,
    itemContent: @Composable (value: Float, iconSize: Dp) -> Unit
) {
    val context = LocalContext.current
    val testValues = config.values
    val iconSize = config.iconSizeDp.dp

    // Map of index -> rating (-1, 0, 1)
    val ratings = remember(config.testName, resetKey) {
        val map = mutableStateMapOf<Int, Int>()

        // Try to load existing state from disk so we don't lose it when swiping pages
        var loaded = false
        try {
            val file = File(context.filesDir, "feedback_fixture_${config.testName}.json")
            if (file.exists()) {
                val json = JSONObject(file.readText())
                val resultsArray = json.getJSONArray("results")
                for (i in 0 until resultsArray.length()) {
                    map[i] = resultsArray.getJSONObject(i).getInt("rating")
                }
                loaded = true
            }
        } catch (e: Exception) {
            Log.e("CyberFeedback", "Failed to load prior state", e)
        }

        if (!loaded) {
            testValues.indices.forEach { map[it] = 0 }
        }
        map
    }

    // Dynamic grid sizing based on the icon size, but clamped to a sensible minimum
    // (e.g. 120dp) so we never create tiny un-tappable columns or overload the screen visually.
    val minCellSize = androidx.compose.ui.unit.max((iconSize * 2.5f), 120.dp)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minCellSize),
        modifier = modifier.cyberComponentSemantics("CyberFeedbackFixture", appendedA11y, customA11y).fillMaxSize().padding(CyberPrimitives.Spacing.dp8)
    ) {
        itemsIndexed(testValues) { index, value ->
            val rating = ratings[index] ?: 0

            // Grades: Green (+1), Red (-1), Black (0)
            val borderColor = when (rating) {
                1 -> CyberPrimitives.Colors.Green500
                -1 -> CyberPrimitives.Colors.Magenta500
                else -> CyberPrimitives.Colors.Chrome600
            }

            val bgColor = when (rating) {
                1 -> CyberPrimitives.Colors.Green500.copy(alpha = 0.15f)
                -1 -> CyberPrimitives.Colors.Magenta500.copy(alpha = 0.15f)
                else -> Color.Black
            }

            Box(
                modifier = Modifier
                    .padding(CyberPrimitives.Spacing.dp8)
                    .aspectRatio(1f) // Keep it perfectly square
                    .graphicsLayer {
                        // Simple drop shadow matching the state color
                        shadowElevation = if (rating != 0) 16f else 4f
                        ambientShadowColor = borderColor
                        spotShadowColor = borderColor
                        shape = RectangleShape
                        clip = false
                    }
                    .background(bgColor)
                    .border(
                        width = CyberPrimitives.BorderWidths.dp2,
                        color = borderColor
                    )
                    .clickable {
                        // Cycle ratings: 0 -> 1 -> -1 -> 0
                        ratings[index] = when (rating) {
                            0 -> 1
                            1 -> -1
                            else -> 0
                        }
                        // Instantly record to file so no user action is needed
                        recordFeedback(context, config.testName, testValues, ratings.toMap())
                    },
                contentAlignment = Alignment.Center
            ) {
                itemContent(value, iconSize)
            }
        }
    }
}

private fun recordFeedback(context: Context, testName: String, testValues: List<Float>, ratings: Map<Int, Int>) {
    try {
        val jsonArray = JSONArray()
        for (i in testValues.indices) {
            val obj = JSONObject()
            obj.put("value", testValues[i].toDouble())
            obj.put("rating", ratings[i] ?: 0)
            jsonArray.put(obj)
        }

        val result = JSONObject()
        result.put("test_name", testName)
        result.put("results", jsonArray)
        result.put("timestamp", System.currentTimeMillis())

        val baseConfig = JSONObject().apply {
            put("GlowPulseDuration", com.example.cyberpunkandroid.config.CyberConfig.Effects.GlowPulseDuration)
            put("PulseMinOpacity", com.example.cyberpunkandroid.config.CyberConfig.Effects.PulseMinOpacity.toDouble())
            put("GlowIntensity", com.example.cyberpunkandroid.config.CyberConfig.Effects.GlowIntensity.toDouble())
            put("PingScale", com.example.cyberpunkandroid.config.CyberConfig.Effects.PingScale.toDouble())
        }
        result.put("base_config", baseConfig)

        val file = File(context.filesDir, "feedback_fixture_${testName}.json")
        file.writeText(result.toString(4))
        Log.d("CyberFeedback", "Recorded test data to: ${file.absolutePath}")

        // Also update a unified ratings.json
        val ratingsFile = File(context.filesDir, "ratings.json")
        val ratingsJson = if (ratingsFile.exists()) {
            JSONObject(ratingsFile.readText())
        } else {
            JSONObject()
        }
        ratingsJson.put(testName, result)
        ratingsFile.writeText(ratingsJson.toString(4))
    } catch (e: Exception) {
        Log.e("CyberFeedback", "Failed to write feedback JSON", e)
    }
}
