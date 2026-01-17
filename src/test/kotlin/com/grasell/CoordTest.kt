package com.grasell

import org.junit.Test
import kotlin.test.assertEquals

class CoordTest {

    @Test
    fun `newCoord creates coord with correct x and y values`() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.x)
        assertEquals(15, coord.y)
    }

    @Test
    fun `downOne increments y by 1`() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.downOne.x)
        assertEquals(16, coord.downOne.y)
    }

    @Test
    fun `upOne decrements y by 1`() {
        val coord = newCoord(10, 15)

        assertEquals(10, coord.upOne.x)
        assertEquals(14, coord.upOne.y)
    }

    @Test
    fun `leftOne decrements x by 1`() {
        val coord = newCoord(10, 15)

        assertEquals(9, coord.leftOne.x)
        assertEquals(15, coord.leftOne.y)
    }

    @Test
    fun `rightOne increments x by 1`() {
        val coord = newCoord(10, 15)

        assertEquals(11, coord.rightOne.x)
        assertEquals(15, coord.rightOne.y)
    }

    @Test
    fun `coord handles negative values correctly`() {
        val coord = newCoord(-5, -10)

        assertEquals(-5, coord.x)
        assertEquals(-10, coord.y)
    }

    @Test
    fun `coord handles zero values correctly`() {
        val coord = newCoord(0, 0)

        assertEquals(0, coord.x)
        assertEquals(0, coord.y)
    }
}
