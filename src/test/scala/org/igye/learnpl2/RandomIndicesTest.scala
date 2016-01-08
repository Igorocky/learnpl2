package org.igye.learnpl2

import org.junit.Assert._
import org.junit.Test

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

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

        @tailrec
        def findMinMaxDif(nums: List[Int], min: Int = Int.MaxValue, max: Int = Int.MinValue): (Int, Int) = {
            val diffMayBeNegative = nums.tail.head - nums.head
            val diff = if (diffMayBeNegative >= 0) diffMayBeNegative else elemsCnt + diffMayBeNegative
            val newMin = if (diff < min) diff else min
            val newMax = if (diff > max) diff else max
            if (nums.size == 2) {
                (newMin, newMax)
            } else {
                findMinMaxDif(nums.tail, newMin, newMax)
            }
        }

        for (i <- 1 to 100) {
            val inds = new RandomIndices().getRandomIndices(elemsCnt, 15).reverse
            assertEquals(150, inds.length)
            val (min, max) = findMinMaxDif(inds.take(inds.length - 15))
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

    @Test
    def testCalcShiftNormalCase(): Unit = {
        val buf = ListBuffer[Int](4, 10, 0)
        val baseIdx = 1
        val left = 0.357
        val middle = 0.143
        val right = 0.5
        assertEquals(-1, RandomIndices.calcShift(baseIdx, buf, left - 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + middle - 0.001))
        assertEquals(1, RandomIndices.calcShift(baseIdx, buf, left + middle + 0.001))
    }

    @Test
    def testCalcShiftLeftBoundaryCase(): Unit = {
        val buf = ListBuffer[Int](4, 10, 0)
        val baseIdx = 0
        val left = 0.5
        val middle = 0.357
        val right = 0.143
        assertEquals(-1, RandomIndices.calcShift(baseIdx, buf, left - 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + middle - 0.001))
        assertEquals(1, RandomIndices.calcShift(baseIdx, buf, left + middle + 0.001))
    }

    @Test
    def testCalcShiftRightBoundaryCase(): Unit = {
        val buf = ListBuffer[Int](4, 10, 0)
        val baseIdx = 2
        val left = 0.143
        val middle = 0.5
        val right = 0.357
        assertEquals(-1, RandomIndices.calcShift(baseIdx, buf, left - 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + 0.001))
        assertEquals(0, RandomIndices.calcShift(baseIdx, buf, left + middle - 0.001))
        assertEquals(1, RandomIndices.calcShift(baseIdx, buf, left + middle + 0.001))
    }

    @Test
    def testSecondVersionOfCalcShiftNormalCase(): Unit = {
        val baseIdx = 1
        assertEquals(-1, RandomIndices.calcShift(baseIdx, ListBuffer[Int](1, 2, 3)))
        assertEquals(0, RandomIndices.calcShift(baseIdx, ListBuffer[Int](2, 1, 3)))
        assertEquals(1, RandomIndices.calcShift(baseIdx, ListBuffer[Int](2, 3, 1)))

        for (i <- 1 to 20) {
            val shift = RandomIndices.calcShift(baseIdx, ListBuffer[Int](2, 1, 1))
            assertTrue(shift == 0 || shift == 1)
        }
    }

    @Test
    def testSecondVersionOfCalcShiftLeftBoundaryCase(): Unit = {
        val buf = ListBuffer[Int](4, 10, 0)
        val baseIdx = 0
        assertEquals(-1, RandomIndices.calcShift(baseIdx, buf))
    }

    @Test
    def testSecondVersionOfCalcShiftRightBoundaryCase(): Unit = {
        val buf = ListBuffer[Int](0, 10, 4)
        val baseIdx = 2
        assertEquals(1, RandomIndices.calcShift(baseIdx, buf))
    }

    @Test
    def testLastWordsCounts(): Unit = {
        val rnd = new RandomIndices
        val elemsCnt = 20
        val pct = 30

        @tailrec
        def findMinMax(nums: List[Int], min: Int = Int.MaxValue, max: Int = Int.MinValue): (Int, Int) = {
            val num = nums.head
            val newMin = if (num < min) num else min
            val newMax = if (num > max) num else max
            if (nums.size == 2) {
                (newMin, newMax)
            } else {
                findMinMax(nums.tail, newMin, newMax)
            }
        }

        @tailrec
        def checkBuf(level: Int, prevBuf: List[Int] = Nil, acumRes: List[Int] = Nil): List[Int] = {
            if (level <= 0) {
                acumRes
            } else {
                val idxs = rnd.getRandomIndices(elemsCnt, pct)
                val buf = rnd.getLastWordsCounts
//                println("------------------------------------------------------------------")
//                println(s"idxs = $idxs")
//                println(s"buf = $buf")
                val (min1, max1) = findMinMax(buf)
                val (min, max) = (min1 - 1, max1 - 1)
                val dif = max - min
//                println(s"MinMax = ${(min, max)}, dif = $dif (${dif/max.toDouble*100}%)")
                assertEquals(elemsCnt, buf.length)
                if (prevBuf.isEmpty) {
                    assertTrue(buf.zipWithIndex.forall{case (c,i) =>
                        if (idxs.contains(i)) c == 2 else c == 1
                    })
                } else {
                    assertTrue(buf.zipWithIndex.forall{case (c,i) =>
                        if (idxs.contains(i)) c == prevBuf(i) + 1 else c == prevBuf(i)
                    })
                }
                checkBuf(level - 1, buf, dif::acumRes)
            }
        }

        val diffs = checkBuf(6)
        assertEquals(6, diffs.length)
        assertTrue(diffs.max <= 2)
    }

    @Test
    def testFindIdxWithMinCnt_emptyList(): Unit = {
        val buf = ListBuffer[Int](1, 2, 1, 2, 3, 1, 4, 3, 3)
        for (i <- 1 to 20) {
            val minCntIdx = RandomIndices.findIdxWithMinCnt(buf, Nil)
            assertTrue(minCntIdx == 0 || minCntIdx == 2 || minCntIdx == 5)
        }
    }

    @Test
    def testFindIdxWithMinCnt_nonemptyList(): Unit = {
        val buf = ListBuffer[Int](1, 2, 1, 2, 3, 1, 4, 3, 3)
        for (i <- 1 to 20) {
            val minCntIdx = RandomIndices.findIdxWithMinCnt(buf, List(0, 2))
            assertEquals(5, minCntIdx)
        }
    }
}
