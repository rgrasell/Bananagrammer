package com.grasell

import org.junit.Test
import kotlin.test.assertEquals

class CoordTest {

    @Test
    public fun test_Constructor() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.x)
        assertEquals(15, coord.y)
    }

    @Test
    public fun test_downOne() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.downOne.x)
        assertEquals(16, coord.downOne.y)
    }

    @Test
    public fun test_upOne() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.upOne.x)
        assertEquals(14, coord.upOne.y)
    }

    @Test
    public fun leftOne() {
        val coord = newCoord(10, 15)

        assertEquals(9, coord.leftOne.x)
        assertEquals(15, coord.leftOne.y)
    }

    @Test
    public fun test_rightOne() {
        val coord = newCoord(10, 15)

        assertEquals(11, coord.rightOne.x)
        assertEquals(15, coord.rightOne.y)
    }
}
