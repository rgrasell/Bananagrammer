package com.grasell

/**
 * Performs [block] on each element of the sequence and returns the original element.
 * Similar to [onEach] but returns a lazy sequence.
 * 
 * Note: Consider using the standard library's [onEach] if immediate execution is acceptable.
 */
fun <T, V> Sequence<T>.interstitial(block: (T) -> V): Sequence<T> {
    return this.map {
        block(it)
        it
    }
}