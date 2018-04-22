package com.grasell

import kotlinx.collections.immutable.ImmutableSet
import kotlin.math.min

// TODO: Tests to ensure that this works. Especially the maxWordLength feature of anyStartWith()
// TODO: Optimization: This class has many inefficient recursive calls
class Dictionary {
    private val root = Node()

    fun isWord(seq: CharSequence) = root.isWord(seq)
    fun anyStartWith(seq: CharSequence, hand: Hand) = root.isPrefix(seq, hand)
    fun add(seq: CharSequence) = root.add(seq)
}

private class Node {
    val subNodes = Array<Node?>('z' - 'a' + 1) { null }
    var smallestLeaf = 0
    var terminal = false

    fun add(word: CharSequence) {
        //we only allow lowercase a-z
        if (word.any { char -> char.isUpperCase() || !char.isLetter() }) {
            throw Exception("Word $word contains an illegal character")
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
        //TODO: Optimization: check if we can possibly make another jump given out hand
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