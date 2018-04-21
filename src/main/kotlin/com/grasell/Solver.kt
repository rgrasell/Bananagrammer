package com.grasell

import kotlinx.collections.immutable.immutableMapOf
import kotlinx.collections.immutable.toImmutableSet

fun solve(tiles: Set<Letter>, dictionary: Dictionary, gameBoard: GameBoard = immutableMapOf()): GameBoard? {

    // If we're out of tiles, check that all words are valid
    if (tiles.isEmpty()) {
        val allValid = gameBoard.allWords().all { dictionary.isWord(it) }
        return if (allValid) gameBoard else null
    }

    val immutableLetters = tiles.toImmutableSet()

    val solution = gameBoard.allValidPlacements()
            .flatMap { coord ->
                immutableLetters.asSequence()
                        .filter { letter ->
                            gameBoard.wordsIfPlaced(coord, letter.char).all { dictionary.anyStartWith(it, immutableLetters.size-1) }
                        }
                        .map { letter -> solve(immutableLetters.remove(letter), dictionary, gameBoard.withTile(letter.char, coord)) }
            }
            .firstOrNull { it != null }

    return solution
}

// Wrapper class that allows us to track multiple instances of the same character in a set
class Letter(val char: Char)

/**
 * Our algorithm works by placing tiles in the bottom or right sides of existing tiles.
 * allValidPlacements() returns all coordinates which are empty and either below or right of an existing tile.
 * If there are no tiles on the board, we start with 1 valid placement in the center.
 */
fun GameBoard.allValidPlacements() =
        if (isEmpty()) sequenceOf(Coord(0, 0))
        else this.asSequence()
                .flatMap { (key, value) -> sequenceOf(key.rightOne, key.downOne) }
                .filter { !this.containsKey(it) }