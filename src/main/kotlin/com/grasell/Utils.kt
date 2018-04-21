package com.grasell

fun <T, V> Sequence<T>.interstitial(block: (T) -> V): Sequence<T> {
    return this.map {
        block(it)
        it
    }
}