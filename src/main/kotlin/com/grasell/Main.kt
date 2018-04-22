package com.grasell

import java.io.File

fun main(args: Array<String>) {
    println("Building dictionary.")
    val dict = buildDictionary()
    println("Dictionary build.")

    val testString = "thisisatest"

    val solution = solve(stringToHand(testString), dict)

    println(solution?.humanReadable() ?: "No solution :(")
}

private fun buildDictionary(): Dictionary {
    val dictionary = Dictionary()

    val inputStream = File("dictionary.txt").inputStream()

    inputStream.bufferedReader().useLines {
        it.map { word -> word.toLowerCase() }
                .forEach { dictionary.add(it) }
    }

    return dictionary
}