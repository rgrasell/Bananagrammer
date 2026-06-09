package com.grasell

import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SolverTest {

    companion object {
        private lateinit var dictionary: Dictionary

        @BeforeClass
        @JvmStatic
        fun buildDictionary() {
            dictionary = Dictionary()
            File("dictionary.txt").bufferedReader().useLines { lines ->
                lines.forEach { dictionary.add(it.lowercase()) }
            }
        }
    }

    @Test
    fun solvesSimpleWord() {
        assertValidSolution("cat", solve(stringToHand("cat"), dictionary))
    }

    @Test
    fun solvesBenchmarkInput() {
        assertValidSolution("ryanalwaysdoit", solve(stringToHand("ryanalwaysdoit"), dictionary))
    }

    @Test
    fun solvesInputsRequiringMultipleWords() {
        for (input in listOf("catdog", "snowyear", "jukebox", "quilted", "pizzazz", "banana")) {
            assertValidSolution(input, solve(stringToHand(input), dictionary))
        }
    }

    @Test
    fun reportsUnsolvableHands() {
        // No vowels and no valid all-consonant arrangement.
        assertNull(solve(stringToHand("zzzz"), dictionary))
        assertNull(solve(stringToHand("qqxjkz"), dictionary))
        // Solvable as two words, but "quiz" and "jam" share no letter to cross on.
        assertNull(solve(stringToHand("quizjam"), dictionary))
    }

    @Test
    fun emptyHandIsTriviallySolved() {
        assertEquals(emptyMap(), solve(stringToHand(""), dictionary))
    }

    @Test
    fun singleTileFormsNoWordsAndSolves() {
        // A lone tile creates no run of length >= 2, so it constrains nothing.
        val solution = solve(stringToHand("a"), dictionary)
        assertNotNull(solution)
        assertEquals(1, solution.size)
    }

    /**
     * A solution is valid when it uses exactly the hand's tiles, every horizontal and
     * vertical run of 2+ letters is a dictionary word, every tile is part of at least
     * one word (unless the board is a single tile), and all tiles form one connected group.
     */
    private fun assertValidSolution(input: String, solution: GameBoard?) {
        assertNotNull(solution, "Expected a solution for '$input'")

        // Exactly the hand's tiles.
        val boardLetters = solution.values.map { it.char }.sorted()
        assertEquals(input.toList().sorted(), boardLetters, "Board letters don't match hand for '$input'")

        // Every maximal run of length >= 2 is a word.
        for ((coord, _) in solution) {
            for ((prev, next) in listOf(
                    Coord::leftOne to Coord::rightOne,
                    Coord::upOne to Coord::downOne)) {
                if (solution.containsKey(prev(coord))) continue  // not the start of a run
                val run = StringBuilder()
                var cursor = coord
                while (solution.containsKey(cursor)) {
                    run.append(solution[cursor]!!.char)
                    cursor = next(cursor)
                }
                if (run.length > 1) {
                    assertTrue(dictionary.isWord(run), "'$run' is not a word (input '$input')")
                }
            }
        }

        // Connectivity.
        val visited = mutableSetOf<Coord>()
        val queue = ArrayDeque<Coord>()
        queue.add(solution.keys.first())
        visited.add(solution.keys.first())
        while (queue.isNotEmpty()) {
            val coord = queue.removeFirst()
            for (neighbor in listOf(coord.leftOne, coord.rightOne, coord.upOne, coord.downOne)) {
                if (solution.containsKey(neighbor) && visited.add(neighbor)) queue.add(neighbor)
            }
        }
        assertEquals(solution.size, visited.size, "Board is not connected for '$input'")
    }
}

class DictionaryTest {

    @Test
    fun recognizesAddedWords() {
        val dict = Dictionary()
        dict.add("cat")
        dict.add("cats")
        assertTrue(dict.isWord("cat"))
        assertTrue(dict.isWord("cats"))
        assertTrue(!dict.isWord("ca"))
        assertTrue(!dict.isWord("catss"))
        assertTrue(!dict.isWord("dog"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsIllegalCharacters() {
        Dictionary().add("Cat")
    }
}
