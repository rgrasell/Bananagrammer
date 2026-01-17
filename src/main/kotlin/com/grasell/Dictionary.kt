package com.grasell

import kotlin.math.min

/**
 * A Trie-based dictionary for efficient word lookups and prefix matching.
 * 
 * This dictionary only supports lowercase a-z characters. Words containing
 * uppercase letters or non-letter characters will be rejected.
 * 
 * Key features:
 * - O(n) lookup time where n is the length of the word
 * - Prefix matching with early termination based on hand size
 * 
 * @see Node for the internal trie node implementation
 */
// TODO: Tests to ensure that this works. Especially the maxWordLength feature of anyStartWith()
// TODO: Optimization: This class has many inefficient recursive calls
class Dictionary {
    private val root = Node()

    /** Checks if the given sequence is a complete word in the dictionary. */
    fun isWord(seq: CharSequence) = root.isWord(seq)
    
    /** Checks if any word in the dictionary starts with the given prefix, considering hand constraints. */
    fun anyStartWith(seq: CharSequence, hand: Hand) = root.isPrefix(seq, hand)
    
    /** Adds a word to the dictionary. Only lowercase a-z characters are allowed. */
    fun add(seq: CharSequence) = root.add(seq)
}

private class Node {
    val subNodes = Array<Node?>('z' - 'a' + 1) { null }
    var smallestLeaf = 0
    var terminal = false

    fun add(word: CharSequence) {
        //we only allow lowercase a-z
        if (word.any { char -> char.isUpperCase() || !char.isLetter() }) {
            throw IllegalArgumentException("Word '$word' contains an illegal character: only lowercase a-z allowed")
        }

        addRecursive(word)
    }

    tailrec fun isWord(seq: CharSequence): Boolean {
        if (seq.isEmpty()) {
            return terminal
        }

        val index = getIndex(seq.first())
        val subNode = subNodes[index] ?: return false

        val nextSeq = withoutFirstChar(seq)
        return subNode.isWord(nextSeq)
    }

    tailrec fun isPrefix(seq: CharSequence, hand: Hand): Boolean {
        if (smallestLeaf > hand.size) return false
        if (seq.isEmpty()) return true
        //TODO: Optimization: check if we can possibly make another jump given our hand
        val index = getIndex(seq.first())
        val subNode = subNodes[index] ?: return false
        val nextSeq = withoutFirstChar(seq)
        return subNode.isPrefix(nextSeq, hand)
    }

    private tailrec fun addRecursive(seq: CharSequence) {
        smallestLeaf = min(smallestLeaf, seq.length)

        if (seq.isEmpty()) {
            terminal = true
            return
        }

        val subIndex = getIndex(seq.first())
        var subNode = subNodes[subIndex]
        if (subNode == null) {
            subNode = Node()
            subNodes[subIndex] = subNode
        }

        val nextSeq = withoutFirstChar(seq)
        subNode.addRecursive(nextSeq)
    }


    private fun getIndex(char: Char): Int {
        return char - 'a'
    }

    // TODO: Optimization: How is subSequence implemented? Hope we aren't copying strings.
    private fun withoutFirstChar(seq: CharSequence): CharSequence {
        return seq.subSequence(1, seq.length)
    }
}