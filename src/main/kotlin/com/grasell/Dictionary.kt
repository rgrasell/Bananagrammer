package com.grasell

import kotlin.math.min

class Dictionary {
    private val root = Node()

    fun isWord(seq: CharSequence) = root.isWord(seq)
    fun anyStartWith(seq: CharSequence, maxWordLength: Int = Int.MAX_VALUE) = root.isPrefix(seq, maxWordLength)
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

    tailrec fun isPrefix(seq: CharSequence, maxWordLength: Int): Boolean {
        if (smallestLeaf > maxWordLength) return false
        if (seq.isEmpty()) return true

        val index = getIndex(seq.first())
        val subNode = subNodes[index] ?: return false
        val nextSeq = withoutFirstChar(seq)
        return subNode.isPrefix(nextSeq, maxWordLength)
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

    private fun withoutFirstChar(seq: CharSequence): CharSequence {
        return seq.subSequence(1, seq.length)
    }
}