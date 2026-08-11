package com.retrotv.app.util

import java.io.File

/**
 * Comparator that sorts strings the "human" way: "Episode 2" before "Episode 10".
 * Splits each string into runs of digits vs. non-digits and compares digit runs
 * numerically instead of lexically.
 */
object NaturalSort : Comparator<String> {
    override fun compare(a: String, b: String): Int {
        val ax = splitChunks(a)
        val bx = splitChunks(b)
        var i = 0
        while (i < ax.size && i < bx.size) {
            val ac = ax[i]
            val bc = bx[i]
            val result = if (ac.isDigits && bc.isDigits) {
                val an = ac.value.toLongOrNull()
                val bn = bc.value.toLongOrNull()
                if (an != null && bn != null) an.compareTo(bn) else ac.value.compareTo(bc.value)
            } else {
                ac.value.compareTo(bc.value, ignoreCase = true)
            }
            if (result != 0) return result
            i++
        }
        return ax.size - bx.size
    }

    private data class Chunk(val value: String, val isDigits: Boolean)

    private fun splitChunks(s: String): List<Chunk> {
        val chunks = mutableListOf<Chunk>()
        var i = 0
        while (i < s.length) {
            val start = i
            val isDigit = s[i].isDigit()
            while (i < s.length && s[i].isDigit() == isDigit) i++
            chunks.add(Chunk(s.substring(start, i), isDigit))
        }
        return chunks
    }
}

/** Sorts a list of Files in natural order by the given name selector (defaults to file name). */
fun List<File>.naturalSortedBy(name: (File) -> String = { it.name }): List<File> =
    sortedWith(Comparator { a, b -> NaturalSort.compare(name(a), name(b)) })
