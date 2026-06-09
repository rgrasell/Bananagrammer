package com.grasell

/**
 * A trie over 'a'..'z'.
 *
 * Nodes are exposed (internally) so the solver can walk the trie directly over its
 * board representation, character by character, without building intermediate strings.
 */
class Dictionary {
    internal val root = Node()

    fun add(word: CharSequence) {
        var node = root
        for (i in word.indices) {
            val c = word[i]
            if (c !in 'a'..'z') throw IllegalArgumentException("Word $word contains an illegal character")

            // Letters still needed to finish some word from this node.
            val lettersLeft = word.length - i
            if (lettersLeft < node.minLettersToWord) node.minLettersToWord = lettersLeft

            val index = c - 'a'
            var child = node.children[index]
            if (child == null) {
                child = Node()
                node.children[index] = child
            }
            node = child
        }
        node.terminal = true
        node.minLettersToWord = 0
    }

    fun isWord(seq: CharSequence): Boolean {
        var node = root
        for (i in seq.indices) {
            node = node.children[seq[i] - 'a'] ?: return false
        }
        return node.terminal
    }
}

internal class Node {
    val children = arrayOfNulls<Node>(26)
    var terminal = false

    /** Minimum number of additional letters needed to reach any complete word from here. */
    var minLettersToWord = Int.MAX_VALUE
}
