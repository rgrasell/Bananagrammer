package com.grasell
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Represents the player's hand of letter tiles.
 * 
 * Implemented as an immutable list of 26 integers, where each index represents
 * a letter (0='a', 1='b', ..., 25='z') and the value is the count of that tile.
 * 
 * Immutability enables safe backtracking in the solver algorithm.
 */
typealias Hand = ImmutableList<Int>

fun Hand.has(c: Char): Boolean {
    return this[getIndex(c)] > 0
}

private fun Hand.getIndex(c : Char):Int {
    return c - 'a'
}

fun Hand.remove(c: Char): Hand {
    if(this[getIndex(c)] == 0) throw IllegalStateException("Cannot remove '$c' from hand: tile not present")

    return this.set(getIndex(c), this[getIndex(c)]-1)
}

//TODO: make this better
fun Hand.noTiles(): Boolean {
    return this.asSequence().all { it == 0 }
}

fun Hand.charSequence(): Sequence<Char> {
    return ('a'..'z').asSequence().zip(this.asSequence())
            .filter { it.second != 0 }
            .map { it.first }
}

fun stringToHand(str: String): Hand {
    var hand = List(26) {0}.toImmutableList()
    str.forEach { hand = hand.add(it) }
    return hand
}

fun Hand.add(c: Char): Hand = this.set(getIndex(c), this[getIndex(c)]+1)