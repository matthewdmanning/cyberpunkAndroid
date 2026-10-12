package com.example.cyberpunkandroid.icons

/*
 * Growable lists of unboxed primitives for the stroke geometry's hot loops. ArrayList<Int> and
 * ArrayList<Double> box every element; these keep plain int, long and double arrays.
 */

internal class IntArrayList(capacity: Int = 8) {
    private var data = IntArray(capacity)
    var size = 0
        private set

    fun add(v: Int) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    operator fun get(i: Int) = data[i]

    fun toArray(): IntArray = data.copyOf(size)
}

internal class DoubleArrayList(capacity: Int = 8) {
    private var data = DoubleArray(capacity)
    var size = 0
        private set

    fun add(v: Double) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    operator fun get(i: Int) = data[i]

    fun toArray(): DoubleArray = data.copyOf(size)
}

internal class LongArrayList(capacity: Int = 64) {
    private var data = LongArray(capacity)
    private var size = 0

    fun add(v: Long) {
        if (size == data.size) data = data.copyOf(size * 2)
        data[size++] = v
    }

    fun sortedUnique(): LongArray {
        val sorted = data.copyOf(size)
        sorted.sort()
        var n = 0
        for (i in sorted.indices) if (i == 0 || sorted[i] != sorted[i - 1]) sorted[n++] = sorted[i]
        return sorted.copyOf(n)
    }
}
