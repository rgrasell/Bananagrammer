package com.grasell

import java.util.Random

fun solve(hand: Hand, dictionary: Dictionary): GameBoard? = Solver(dictionary, hand).solve()

/**
 * Depth-first search over tile placements, like the original algorithm: tiles are placed
 * in empty cells directly right of or below an existing tile (starting from a single seed
 * cell), and a placement is kept only if every word fragment it creates can still become
 * a dictionary word.
 *
 * Unlike the original, the search state is fully mutable and updated in place:
 *  - the board is a flat CharArray grid (with a one-cell empty border so neighbor scans
 *    never need bounds checks), placements are undone on backtrack,
 *  - the hand is an IntArray of letter counts,
 *  - word fragments are validated by walking the trie directly over the grid; no Strings,
 *    maps, or sequences are allocated anywhere in the search.
 *
 * Two prunes the original couldn't do:
 *  - Failed-state memoization: different placement orders reach identical boards, and a
 *    board fully determines the remaining search, so each failed board (and its transpose,
 *    by symmetry) is recorded in a hash set keyed by an incrementally-maintained Zobrist
 *    hash and never explored twice.
 *  - Hand-aware completability: a fragment isn't just checked to be a prefix of some word;
 *    the trie is probed to confirm the word can actually be finished with the letters still
 *    available (remaining hand tiles plus any board tiles further along the same line).
 */
private class Solver(dictionary: Dictionary, hand: Hand) {
    private val root = dictionary.root

    private val tileCount = hand.totalTiles

    // Every placement is right of or below an existing tile starting from (0,0), so live
    // cells fit in tileCount x tileCount; pad with a border of empties on all sides.
    private val stride = tileCount + 2
    private val grid = CharArray(stride * stride)

    private val handCounts = hand.counts.copyOf()
    private var remaining = tileCount

    // Grid indices of placed tiles, in placement order, for candidate enumeration and undo.
    private val placedStack = IntArray(tileCount)
    private var depth = 0

    // Zobrist hashing: hash is XOR of a random number per (cell, letter) pair. transposedHash
    // tracks the same board flipped across the diagonal; min() of the two canonicalizes, so a
    // board and its mirror image share one memo entry.
    private val zobrist = LongArray(stride * stride * 26).also {
        val random = Random(0x5DEECE66D)
        for (i in it.indices) it[i] = random.nextLong()
    }
    private var hash = 0L
    private var transposedHash = 0L
    private val failedStates = LongHashSet()

    // Per-cell stamps for deduplicating candidate cells without clearing an array each call.
    private val candidateStamp = IntArray(stride * stride)
    private var stampGeneration = 0

    // Per-depth candidate buffers so dfs() never allocates.
    private val candidateBuffers = Array(tileCount) { IntArray(2 * tileCount + 1) }

    // Scratch letter pool for completability probes.
    private val pool = IntArray(26)

    private val seedCell = stride + 1  // grid coordinate (0, 0)

    fun solve(): GameBoard? {
        if (tileCount == 0) return emptyMap()
        if (!dfs()) return null

        val solution = HashMap<Coord, Tile>()
        for (i in 0 until depth) {
            val cell = placedStack[i]
            solution[newCoord(cell % stride - 1, cell / stride - 1)] = Tile(grid[cell])
        }
        return solution
    }

    private fun dfs(): Boolean {
        if (remaining == 0) return allWordsValid()

        val canonicalHash = minOf(hash, transposedHash)
        if (failedStates.contains(canonicalHash)) return false

        // Collect candidate cells: empty cells right of or below an existing tile.
        val candidates = candidateBuffers[depth]
        var candidateCount = 0
        if (depth == 0) {
            candidates[candidateCount++] = seedCell
        } else {
            if (stampGeneration == Int.MAX_VALUE) {
                candidateStamp.fill(0)
                stampGeneration = 0
            }
            stampGeneration++
            for (i in 0 until depth) {
                val tileCell = placedStack[i]
                val right = tileCell + 1
                if (grid[right] == EMPTY && candidateStamp[right] != stampGeneration) {
                    candidateStamp[right] = stampGeneration
                    candidates[candidateCount++] = right
                }
                val down = tileCell + stride
                if (grid[down] == EMPTY && candidateStamp[down] != stampGeneration) {
                    candidateStamp[down] = stampGeneration
                    candidates[candidateCount++] = down
                }
            }
        }

        for (c in 0 until candidateCount) {
            val cell = candidates[c]
            for (letter in 0 until 26) {
                if (handCounts[letter] == 0) continue
                val char = 'a' + letter

                place(cell, char, letter)
                if (lineViable(cell, 1) && lineViable(cell, stride) && dfs()) return true
                unplace(cell, char, letter)
            }
        }

        failedStates.add(canonicalHash)
        return false
    }

    private fun place(cell: Int, char: Char, letter: Int) {
        grid[cell] = char
        handCounts[letter]--
        remaining--
        placedStack[depth++] = cell
        hash = hash xor zobrist[cell * 26 + letter]
        transposedHash = transposedHash xor zobrist[transposed(cell) * 26 + letter]
    }

    private fun unplace(cell: Int, char: Char, letter: Int) {
        grid[cell] = EMPTY
        handCounts[letter]++
        remaining++
        depth--
        hash = hash xor zobrist[cell * 26 + letter]
        transposedHash = transposedHash xor zobrist[transposed(cell) * 26 + letter]
    }

    private fun transposed(cell: Int) = (cell % stride) * stride + (cell / stride)

    /**
     * Checks the word fragment running through [cell] along [step] (1 = horizontal,
     * stride = vertical). Viable if it's a single letter, already a word, or a prefix
     * completable with the letters still available.
     */
    private fun lineViable(cell: Int, step: Int): Boolean {
        var start = cell
        while (grid[start - step] != EMPTY) start -= step
        var end = cell
        while (grid[end + step] != EMPTY) end += step
        if (start == end) return true

        var node = root
        var i = start
        while (true) {
            node = node.children[grid[i] - 'a'] ?: return false
            if (i == end) break
            i += step
        }
        if (node.terminal) return true

        // The fragment must be finishable by appending letters. Available letters are the
        // remaining hand, plus any tiles already on the board further along this line
        // (placements can fill a gap and merge fragments).
        System.arraycopy(handCounts, 0, pool, 0, 26)
        var budget = remaining
        var beyond = end + step
        // Stop at the grid border (always EMPTY... but cells past a gap may be occupied).
        val lineEnd = if (step == 1) (cell / stride + 1) * stride else grid.size
        while (beyond < lineEnd) {
            val ch = grid[beyond]
            if (ch != EMPTY) {
                pool[ch - 'a']++
                budget++
            }
            beyond += step
        }

        return canComplete(node, budget)
    }

    private fun canComplete(node: Node, budget: Int): Boolean {
        if (node.terminal) return true
        if (node.minLettersToWord > budget) return false
        for (letter in 0 until 26) {
            if (pool[letter] == 0) continue
            val child = node.children[letter] ?: continue
            pool[letter]--
            val ok = canComplete(child, budget - 1)
            pool[letter]++
            if (ok) return true
        }
        return false
    }

    private fun allWordsValid(): Boolean {
        for (i in 0 until depth) {
            val cell = placedStack[i]
            if (grid[cell - 1] == EMPTY && grid[cell + 1] != EMPTY && !runIsWord(cell, 1)) return false
            if (grid[cell - stride] == EMPTY && grid[cell + stride] != EMPTY && !runIsWord(cell, stride)) return false
        }
        return true
    }

    private fun runIsWord(start: Int, step: Int): Boolean {
        var node = root
        var i = start
        while (grid[i] != EMPTY) {
            node = node.children[grid[i] - 'a'] ?: return false
            i += step
        }
        return node.terminal
    }

    companion object {
        private const val EMPTY = '\u0000'  // CharArray default value, so an all-empty grid needs no init
    }
}

/**
 * Minimal open-addressing hash set of Longs: no boxing, no per-entry objects.
 */
private class LongHashSet {
    private var table = LongArray(1 shl 16)
    private var mask = table.size - 1
    private var size = 0
    private var containsZero = false

    fun contains(key: Long): Boolean {
        if (key == 0L) return containsZero
        var index = mix(key) and mask
        while (true) {
            val entry = table[index]
            if (entry == key) return true
            if (entry == 0L) return false
            index = (index + 1) and mask
        }
    }

    fun add(key: Long) {
        if (key == 0L) {
            containsZero = true
            return
        }
        if ((size + 1) * 2 > table.size) grow()
        insert(table, mask, key)
        size++
    }

    private fun insert(table: LongArray, mask: Int, key: Long) {
        var index = mix(key) and mask
        while (true) {
            val entry = table[index]
            if (entry == key) return
            if (entry == 0L) {
                table[index] = key
                return
            }
            index = (index + 1) and mask
        }
    }

    private fun grow() {
        val newTable = LongArray(table.size * 2)
        val newMask = newTable.size - 1
        for (entry in table) {
            if (entry != 0L) insert(newTable, newMask, entry)
        }
        table = newTable
        mask = newMask
    }

    // Finalizer from SplitMix64; Zobrist hashes are already random but cheap insurance.
    private fun mix(key: Long): Int {
        var k = key
        k = (k xor (k ushr 33)) * -0x7ee3623a03d4d6a3L
        k = (k xor (k ushr 29)) * -0x3b314601e57a13adL
        return (k xor (k ushr 32)).toInt()
    }
}
