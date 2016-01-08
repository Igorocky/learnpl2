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
        assertEquals(73, rnd.getRandomIndices(73, 100).size)
    }

    @Test
    def testGetRandomIndicesStep(): Unit = {
        val elemsCnt = 1000

        def findMinMaxDif(min: Int, max: Int, nums: List[Int]): (Int, Int) = {
            val diffMayBeNegative = nums.tail.head - nums.head
            val diff = if (diffMayBeNegative >= 0) diffMayBeNegative else elemsCnt + diffMayBeNegative
            val newMin = if (diff < min) diff else min
            val newMax = if (diff > max) diff else max
            if (nums.size == 2) {
                (newMin, newMax)
            } else {
                findMinMaxDif(newMin, newMax, nums.tail)
            }
        }

        for (i <- 1 to 100) {
            val inds = new RandomIndices().getRandomIndices(elemsCnt, 15).reverse
            assertEquals(150, inds.length)
            val (min, max) = findMinMaxDif(Int.MaxValue, Int.MinValue, inds.take(inds.length - 15))
            assertEquals(6, min)
            assertEquals(8, max)
        }
    }

    @Test
    def testGetRandomIndicesUniqueness(): Unit = {
        val rnd = new RandomIndices
        for (i <- 1 to 100) {
            val res = rnd.getRandomIndices(10, 35)
            assertEquals(4, res.length)
            assertEquals(res.length, res.toSet.size)
        }
    }
}
