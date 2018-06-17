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

//TODO: Possibly memoize results?
private fun GameBoard.horizontalWord(coord: Coord, newChar: Char) = wordFromLeft(coord.leftOne) + newChar + wordToRight(coord.rightOne)

private fun GameBoard.verticalWord(coord: Coord, newChar: Char) = wordFromTop(coord.upOne) + newChar + wordToBottom(coord.downOne)

// TODO: Optimization: need a more efficient way to build up the word representatino than concatting strings
private fun GameBoard.wordFromLeft(coord: Coord) =
        buildWord(coord, { it.leftOne }, { char, string -> char + string } )

private fun GameBoard.wordToRight(coord: Coord): String {
    val tile = this[coord] ?: return ""

    return tile.char + wordToRight(coord.rightOne)
}

private fun GameBoard.wordFromTop(coord: Coord) =
        buildWord(coord, { it.upOne }, { char, string -> char + string })

private fun GameBoard.wordToBottom(coord: Coord): String {
    return buildWord(coord, { it.downOne }, { char, string -> string + char })
}

private fun GameBoard.buildWord(coord: Coord, nextCoord: (Coord) -> Coord, reduce: (Char, String) -> String): String {
    var cursor = coord
    var tile = this[cursor]
    var word = ""

    while (tile != null) {
        word = reduce(tile.char, word)
        cursor = nextCoord(cursor)
        tile = this[cursor]
    }

    return word
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