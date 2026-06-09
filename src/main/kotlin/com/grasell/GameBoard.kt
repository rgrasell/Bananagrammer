package com.grasell

/**
 * Coord packs 2 Int dimensions (x, y) into 1 Long to avoid allocating an object per coordinate.
 * We use bit operations to extract the Int values when requested.
 */
typealias Coord = Long
val Coord.x: Int
    get() = (0xFFFFFFFF and this).toInt()
val Coord.y: Int
    get() = ((this shr 32) and 0xFFFFFFFF).toInt()
val Coord.upOne get() = newCoord(x, y - 1)
val Coord.downOne get() = newCoord(x, y + 1)
val Coord.leftOne get() = newCoord(x - 1, y)
val Coord.rightOne get() = newCoord(x + 1, y)

/**
 * Emulate a constructor for our new fake class.
 */
fun newCoord(x: Int, y: Int): Coord {
    val low = x.toLong() and 0xFFFFFFFF
    val high = y.toLong() shl 32
    return low or high
}

data class Tile(val char: Char)

/**
 * The solved-board result type. The solver itself works on a flat mutable grid for speed
 * and only materializes one of these maps for the final solution.
 */
typealias GameBoard = Map<Coord, Tile>

fun GameBoard.humanReadable(): String {
    if (this.isEmpty()) return "[Empty board]"

    var smallestX = Int.MAX_VALUE
    var biggestX = Int.MIN_VALUE
    var smallestY = Int.MAX_VALUE
    var biggestY = Int.MIN_VALUE
    for (coord in keys) {
        if (coord.x < smallestX) smallestX = coord.x
        if (coord.x > biggestX) biggestX = coord.x
        if (coord.y < smallestY) smallestY = coord.y
        if (coord.y > biggestY) biggestY = coord.y
    }

    return (smallestY..biggestY).joinToString(separator = "\n") { y ->
        (smallestX..biggestX).joinToString(separator = " ") { x ->
            (this[newCoord(x, y)]?.char ?: ' ').toString()
        }
    }
}
