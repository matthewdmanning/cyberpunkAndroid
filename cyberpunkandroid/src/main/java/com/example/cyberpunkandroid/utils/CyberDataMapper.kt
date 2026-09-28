package com.example.cyberpunkandroid.utils

/**
 * Generates a list of data points by evaluating a [mappingFunction] over an evenly spaced 
 * sequence of values within [inputBounds], and coercing (clamping) the results to [outputBounds].
 *
 * This is useful for generating constrained data arrays for charts, waveform visualizers,
 * or procedural UI geometries in the Cyberpunk library.
 *
 * @property inputBounds The domain range (min..max) over which the function will be evaluated.
 * @property outputBounds The bounds (min..max) to which the resulting values will be clamped.
 * @property numPoints The total number of discrete data points to generate in the output list.
 * @property mappingFunction The mathematical function `f(x)` to evaluate at each step.
 */
class CyberDataMapper(
    val inputBounds: ClosedFloatingPointRange<Float>,
    val outputBounds: ClosedFloatingPointRange<Float>,
    val numPoints: Int,
    val mappingFunction: (Float) -> Float
) {
    /**
     * Executes the mapping across the defined intervals and returns the coerced values.
     */
    fun generate(): List<Float> {
        if (numPoints <= 0) return emptyList()
        if (numPoints == 1) {
            return listOf(mappingFunction(inputBounds.start).coerceIn(outputBounds))
        }

        val step = (inputBounds.endInclusive - inputBounds.start) / (numPoints - 1)
        return List(numPoints) { index ->
            val x = inputBounds.start + index * step
            mappingFunction(x).coerceIn(outputBounds)
        }
    }
}
