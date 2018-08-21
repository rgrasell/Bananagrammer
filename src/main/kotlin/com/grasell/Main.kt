package com.grasell

import java.io.File
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    println("Building dictionary.")
    val dict = buildDictionary()
    println("Dictionary built.")

    val testString = "ryanalwaysdoit"

    val time = measureTimeMillis {
        println(solve(stringToHand(testString), dict)?.humanReadable() ?: "No solution :(")
    }

    println("Solved in $time millis.")

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