package org.igye.learnpl2

import org.junit.Assert._
import org.junit.Test

class RandomIndicesTest {
    @Test
    def testGetRandomIndicesLength(): Unit = {
        val rnd = new RandomIndices
        assertEquals(3, rnd.getRandomIndices(9, 30).size)
        assertEquals(2, rnd.getRandomIndices(8, 30).size)
        assertEquals(1, rnd.getRandomIndices(15, 0).size)
    }
}
