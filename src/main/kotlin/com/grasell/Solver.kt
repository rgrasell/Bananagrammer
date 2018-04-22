package com.grasell

import kotlinx.collections.immutable.immutableMapOf
import kotlinx.collections.immutable.toImmutableSet

fun solve(hand: Hand, dictionary: Dictionary, gameBoard: GameBoard = immutableMapOf()): GameBoard? {

    // If we're out of hand, check that all words are valid
    if (hand.noTiles()) {
        val allValid = gameBoard.allWords().all { dictionary.isWord(it) }
        return if (allValid) gameBoard else null
    }

    val solution = gameBoard.allValidPlacements()
            .flatMap { coord ->
                hand.charSequence()
                        .filter { letter ->
                            gameBoard.wordsIfPlaced(coord, letter).all { dictionary.anyStartWith(it, hand) }
                        }
                        .map { letter -> solve(hand.remove(letter), dictionary, gameBoard.withTile(letter, coord)) }
            }
            .firstOrNull { it != null }

    return solution
}


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