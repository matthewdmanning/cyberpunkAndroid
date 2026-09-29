package com.example.cyberpunkandroid

import com.example.cyberpunkandroid.components.terminalGlyphBoundaries
import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks that typing preserves Unicode glyphs and handles empty output. */
class CyberTerminalBackgroundTest {
    /** Use this function to verify glyph boundaries for line art, combining marks, and surrogate pairs. */
    @Test
    fun typingPreservesWholeGlyphs() {
        val text = "┌e\u0301\uD83D\uDE00\n"
        val boundaries = terminalGlyphBoundaries(text)
        assertEquals(listOf("┌", "e\u0301", "\uD83D\uDE00", "\n"), boundaries.zipWithNext { start, end -> text.substring(start, end) })
        assertEquals(listOf(0), terminalGlyphBoundaries(""))
    }
}
