package com.grasell

/**
 * A multiset of tiles, represented as counts per letter 'a'..'z'.
 */
class Hand(internal val counts: IntArray) {
    init {
        require(counts.size == 26) { "Hand requires exactly 26 letter counts" }
    }

    val totalTiles: Int get() = counts.sum()
}

fun stringToHand(str: String): Hand {
    val counts = IntArray(26)
    str.forEach {
        require(it in 'a'..'z') { "Hand string $str contains an illegal character" }
        counts[it - 'a']++
    }
    return Hand(counts)
}
