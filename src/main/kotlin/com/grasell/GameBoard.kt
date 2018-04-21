package com.grasell

import kotlinx.collections.immutable.ImmutableMap

// TODO: Optimization: Creating a billion little Coord objects is hard on GC.  We might be able to get rid of this class.
data class Coord(val x: Int, val y: Int) {
    val upOne get() = Coord(x, y-1)
    val downOne get() = Coord(x, y+1)
    val leftOne get() = Coord(x-1, y)
    val rightOne get() = Coord(x+1, y)
}

// TODO: Optimization: If we get rid of Coord, how will we represent a game board?
// Consider a linked graph with han anchor at (0,0)
typealias GameBoard = ImmutableMap<Coord, Tile>

data class Tile(val char: Char)

fun GameBoard.withTile(char: Char?, coord: Coord): ImmutableMap<Coord, Tile> {
    if (char == null) return this

    return this.put(coord, Tile(char))
}

fun GameBoard.wordsIfPlaced(coord: Coord, newChar: Char): Sequence<String> {
    val seq = sequenceOf(horizontalWord(coord, newChar), verticalWord(coord, newChar))
            .filter { it.length > 1 }
    return seq
}

private fun GameBoard.horizontalWord(coord: Coord, newChar: Char) = wordFromLeft(coord.leftOne) + newChar + wordToRight(coord.rightOne)

private fun GameBoard.verticalWord(coord: Coord, newChar: Char) = wordFromTop(coord.upOne) + newChar + wordToBottom(coord.downOne)

// TODO: Optimization: the following 4 methods recurse inefficiently
private fun GameBoard.wordFromLeft(coord: Coord): String {
    val tile = this[coord] ?: return ""

    return wordFromLeft(coord.leftOne) + tile.char
}

private fun GameBoard.wordToRight(coord: Coord): String {
    val tile = this[coord] ?: return ""

    return tile.char + wordToRight(coord.rightOne)
}

private fun GameBoard.wordFromTop(coord: Coord): String {
    val tile = this[coord] ?: return ""

    return wordFromTop(coord.upOne) + tile.char
}

private fun GameBoard.wordToBottom(coord: Coord): String {
    val tile = this[coord] ?: return ""

    return tile.char + wordToBottom(coord.downOne)
}

// TODO: Optimization: less hack please
// This implementation is looking at every tile, generating the words it touches, then removing duplicates at the end.
// There must be a better way
fun GameBoard.allWords(): Sequence<String> = this.asSequence()
        .flatMap { wordsIfPlaced(it.key, it.value.char) }
        .distinct()

// TODO: Optimization: Iterate through only once to find min/max of each coordinate
// This is low priority though.  It's only called to display output of the algorithm.
fun GameBoard.humanReadable(): String {
    if (this.isEmpty()) return "[Empty board]"

    val smallestX = this.asSequence().minBy { it.key.x }!!.key.x
    val biggestX = this.asSequence().maxBy { it.key.x }!!.key.x
    val smallestY = this.asSequence().minBy { it.key.y }!!.key.y
    val biggestY = this.asSequence().maxBy { it.key.y }!!.key.y

    return (smallestY..biggestY).asSequence().map { y ->
        (smallestX..biggestX).asSequence()
                .map { x ->
                    this[Coord(x, y)]?.char ?: ' '
                }
                .joinToString(separator = " ")
    }.joinToString(separator = "\n")


}