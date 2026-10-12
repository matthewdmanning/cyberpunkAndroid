package com.example.cyberpunkandroid.sliderpanel

import com.example.cyberpunkandroid.sliderpanel.model.MarkSpec
import com.example.cyberpunkandroid.sliderpanel.model.MarksSpec
import com.example.cyberpunkandroid.sliderpanel.model.PanelMode
import com.example.cyberpunkandroid.sliderpanel.model.PinEdge
import com.example.cyberpunkandroid.sliderpanel.model.ScalePoint
import com.example.cyberpunkandroid.sliderpanel.model.ScaleSpec
import com.example.cyberpunkandroid.sliderpanel.model.SliderSpec
import com.example.cyberpunkandroid.sliderpanel.model.SpecParseResult
import com.example.cyberpunkandroid.sliderpanel.model.hashMarks
import com.example.cyberpunkandroid.sliderpanel.model.parseSliderPanelSpec
import com.example.cyberpunkandroid.sliderpanel.model.positionOf
import com.example.cyberpunkandroid.sliderpanel.model.snap
import com.example.cyberpunkandroid.sliderpanel.model.toJson
import com.example.cyberpunkandroid.sliderpanel.model.valueAt
import com.example.cyberpunkandroid.sliderpanel.state.SliderPanelEvent
import com.example.cyberpunkandroid.sliderpanel.state.SliderPanelStore
import com.example.cyberpunkandroid.sliderpanel.state.initialSliderPanelState
import com.example.cyberpunkandroid.sliderpanel.state.reduce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SliderPanelTest {

    private val json = """
        {
          "title": "Lab", "mode": "pinned", "pinEdge": "end", "scrollable": true, "maxHeightDp": 200,
          "sliders": [
            { "id": "t", "label": "Thickness", "unit": "dp", "start": 0.5, "stop": 64, "initial": 4, "step": 0.5,
              "decimals": 1, "scale": { "type": "log" }, "marks": { "minor": 8 } },
            { "id": "w", "label": "Width", "start": 0, "stop": 300, "marks": { "every": 100, "minor": 1 } },
            { "id": "g", "label": "Gain", "unit": "dB", "start": -60, "stop": 12,
              "scale": { "type": "piecewise", "points": [
                { "position": 0, "value": -60 }, { "position": 0.5, "value": -12 }, { "position": 1, "value": 12 } ] },
              "marks": { "values": [{ "value": 0, "label": "0 dB" }] } }
          ]
        }
    """.trimIndent()

    private fun spec() = (parseSliderPanelSpec(json) as SpecParseResult.Success).spec

    @Test
    fun parsesEveryField() {
        val spec = spec()
        assertEquals(PanelMode.Pinned, spec.mode)
        assertEquals(PinEdge.End, spec.pinEdge)
        assertTrue(spec.scrollable)
        assertEquals(ScaleSpec.Log, spec.sliders[0].scale)
        assertEquals(ScaleSpec.Linear, spec.sliders[1].scale)
        assertEquals(3, (spec.sliders[2].scale as ScaleSpec.Piecewise).points.size)
        assertEquals(spec, (parseSliderPanelSpec(spec.toJson()) as SpecParseResult.Success).spec)
    }

    @Test
    fun reportsEveryProblemWithItsPath() {
        val bad = """
            { "sliders": [
              { "id": "a", "label": "A", "start": 0, "stop": 10, "initial": 20, "step": -1, "scale": { "type": "log" } },
              { "id": "a", "label": "B", "start": 5, "stop": 5 }
            ] }
        """.trimIndent()
        val errors = (parseSliderPanelSpec(bad) as SpecParseResult.Failure).errors
        assertTrue(errors.any { "used more than once" in it })
        assertTrue(errors.any { it.startsWith("sliders[0] (a).initial") })
        assertTrue(errors.any { it.startsWith("sliders[0] (a).step") })
        assertTrue(errors.any { it.startsWith("sliders[0] (a).scale: log") })
        assertTrue(errors.any { it.startsWith("sliders[1] (a).start/stop: must differ") })
    }

    @Test
    fun misspelledKeysAndBrokenJsonFailInsteadOfDefaulting() {
        assertTrue(parseSliderPanelSpec("""{ "sliders": [], "scrolable": true }""") is SpecParseResult.Failure)
        assertTrue(parseSliderPanelSpec("""{ "sliders": [""") is SpecParseResult.Failure)
        val wrongType = """{ "sliders": [{ "id": "a", "label": "A", "start": 0, "stop": 1, "scale": { "type": "cubic" } }] }"""
        assertTrue(parseSliderPanelSpec(wrongType) is SpecParseResult.Failure)
    }

    @Test
    fun scalesMapBothWays() {
        val specs = listOf(
            SliderSpec("lin", "", 10.0, -10.0),
            SliderSpec("log", "", 1.0, 1000.0, scale = ScaleSpec.Log),
            SliderSpec("pow", "", 0.0, 8.0, scale = ScaleSpec.Power(3.0)),
            SliderSpec("pw", "", 0.0, 100.0, scale = ScaleSpec.Piecewise(listOf(ScalePoint(0.0, 0.0), ScalePoint(0.8, 10.0), ScalePoint(1.0, 100.0))))
        )
        for (s in specs) for (t in listOf(0.0, 0.1, 0.5, 0.8, 0.95, 1.0)) {
            assertEquals("${s.id} at $t", t, s.positionOf(s.valueAt(t)), 1e-9)
        }
        assertEquals(10.0, specs[1].valueAt(1.0 / 3.0), 1e-9)
        assertEquals(1.0, specs[2].valueAt(0.5), 1e-9)
        assertEquals(10.0, specs[3].valueAt(0.8), 1e-9)
        assertEquals(0.0, specs[0].valueAt(0.5), 1e-9)
    }

    @Test
    fun snapsToStepsFromStartAndStaysInRange() {
        val s = SliderSpec("s", "", 1.0, 10.0, step = 2.0)
        assertEquals(3.0, s.snap(3.9), 0.0)
        assertEquals(9.0, s.snap(10.0), 0.0) // 11 would be past the end
        assertEquals(1.0, s.snap(-5.0), 0.0)
    }

    @Test
    fun logMarksDefaultToDecadesWithClassicMinorTicks() {
        val s = SliderSpec("s", "", 1.0, 1000.0, scale = ScaleSpec.Log, decimals = 0, marks = MarksSpec(minor = 8))
        val marks = s.hashMarks()
        assertEquals(listOf("1", "10", "100", "1000"), marks.filter { it.major }.map { it.label })
        assertEquals(3 * 8, marks.count { !it.major })
        assertEquals(s.positionOf(2.0).toFloat(), marks[1].position, 1e-6f) // first minor is at 2
        assertTrue(marks.zipWithNext().all { (a, b) -> b.position > a.position })
    }

    @Test
    fun everyMarksIncludeTheEndAndRespectDirection() {
        val s = SliderSpec("s", "", 300.0, 0.0, decimals = 0, marks = MarksSpec(every = 100.0))
        assertEquals(listOf("300", "200", "100", "0"), s.hashMarks().map { it.label })
        val labeled = SliderSpec("s", "", 0.0, 1.0, marks = MarksSpec(values = listOf(MarkSpec(0.5, "HALF")), labels = true))
        assertEquals(listOf("HALF"), labeled.hashMarks().map { it.label })
    }

    @Test
    fun reducerMovesSnapsAndResets() {
        var state = initialSliderPanelState(spec())
        assertEquals(4.0, state.values["t"]!!, 0.0)
        assertEquals("4.0 dp", state.sliders[0].valueText)

        state = state.reduce(SliderPanelEvent.PositionChanged("t", 1f))
        assertEquals(64.0, state.values["t"]!!, 0.0)
        state = state.reduce(SliderPanelEvent.ValueChanged("t", 7.3))
        assertEquals(7.5, state.values["t"]!!, 0.0) // snapped to 0.5 from 0.5
        assertEquals(state.sliders[0].spec.positionOf(7.5).toFloat(), state.sliders[0].position, 0f)

        state = state.reduce(SliderPanelEvent.TogglePinned).reduce(SliderPanelEvent.Moved(12f, 34f))
        assertEquals(PanelMode.Floating, state.mode)
        assertEquals(34f, state.offsetYDp, 0f)

        state = state.reduce(SliderPanelEvent.Reset)
        assertEquals(4.0, state.values["t"]!!, 0.0)
        assertEquals(0.0, state.values["w"]!!, 0.0)
        assertSame(state, state.reduce(SliderPanelEvent.PositionChanged("missing", 0.5f)))
    }

    @Test
    fun storePublishesEachStateAndRestoresValues() {
        val store = SliderPanelStore(spec(), values = mapOf("w" to 150.0))
        assertEquals(150.0, store.state.value.values["w"]!!, 0.0)
        store.onEvent(SliderPanelEvent.PositionChanged("g", 0.5f))
        assertEquals(-12.0, store.state.value.values["g"]!!, 1e-9)
        assertEquals("-12.00 dB", store.state.value.sliders[2].valueText)
    }
}
