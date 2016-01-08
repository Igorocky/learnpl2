package org.igye.learnpl2

import java.util.Random

import org.igye.learnpl2.models.Word

import scala.collection.mutable.ListBuffer

class RandomIndices {
    import RandomIndices._

    private var lastWordsCounts = List[Int]()

    def getRandomIndices(elemsCnt: Int, pct: Int): List[Int] = {
        if (elemsCnt != lastWordsCounts.length) {
            lastWordsCounts = (1 to elemsCnt).map(i => 1).toList
        }
        val res = (if (pct <= 50) {
            getRandomIndicesUnder50(elemsCnt, pct, lastWordsCounts)
        } else if (pct < 100) {
            (0 until elemsCnt).toList diff getRandomIndicesUnder50(elemsCnt, 100 - pct, lastWordsCounts.map(-_))
        } else {
            (0 until elemsCnt).toList
        })

        lastWordsCounts = lastWordsCounts.zipWithIndex.map{case (c,i) => if (res.contains(i)) c + 1 else c}
//                pr(s"getRandomIndices.res = $res")
        res
    }

    def findSuitableIndices(words: List[Word], pct: Int): List[Int] = {
        def findBestIndices(num: Int, bestIndices: List[Int], avgForBest: Option[Double] = None): List[Int] = {
            if (num == 0) {
                bestIndices
            } else {
                val cand = getRandomIndices(words.length, pct)
                val avgForBestDbl = avgForBest.getOrElse{
                    bestIndices.map(words(_).text.length).sum.toDouble/bestIndices.length
                }
                val avgForCand = cand.map(words(_).text.length).sum.toDouble/cand.length
                if (avgForCand > avgForBestDbl) {
                    findBestIndices(num - 1, cand, Some(avgForCand))
                } else {
                    findBestIndices(num - 1, bestIndices, Some(avgForBestDbl))
                }
            }
        }
        findBestIndices(10, getRandomIndices(words.length, pct))
    }

    def getLastWordsCounts: List[Int] = lastWordsCounts
}

object RandomIndices {
//    protected[learnpl2] val pr = println(_:Any)

    private val rnd = new Random()

    protected[learnpl2] def calcShift(baseIdx: Int, lastWordsCounts: ListBuffer[Int], shiftPot: Double): Int = {
        val leftIdx = if (baseIdx > 0) baseIdx - 1 else lastWordsCounts.length - 1
        val rightIdx = if (baseIdx < lastWordsCounts.length - 1) baseIdx + 1 else 0
        val sumCnt = (lastWordsCounts(leftIdx) + lastWordsCounts(baseIdx) + lastWordsCounts(rightIdx)).toDouble
        val leftShiftPct = (1 - lastWordsCounts(leftIdx) / sumCnt) / 2
        val rightShiftPct = 1 - (1 - lastWordsCounts(rightIdx) / sumCnt) / 2
        //        pr(s"($leftIdx) -> ${lastWordsCounts(leftIdx)}, ($baseIdx) -> ${lastWordsCounts(baseIdx)} ($rightIdx) -> ${lastWordsCounts(rightIdx)}")
        //        pr(s"sumCnt = $sumCnt")
        //        pr(s"leftShiftPct = $leftShiftPct")
        //        pr(s"rightShiftPct = $rightShiftPct")
        //        pr(s"shiftPot = $shiftPot")
        val res = if (shiftPot < leftShiftPct) {
            -1
        } else if (shiftPot > rightShiftPct) {
            1
        } else {
            0
        }
        //        pr(s"calcShift.res = $res")
        res
    }

    protected[learnpl2] def calcShift(baseIdx: Int, lastWordsCounts: List[Int]): Int = {
        val leftIdx = if (baseIdx > 0) baseIdx - 1 else lastWordsCounts.length - 1
        val rightIdx = if (baseIdx < lastWordsCounts.length - 1) baseIdx + 1 else 0
        findIdxWithMinCnt(
            List(
                lastWordsCounts(leftIdx),
                lastWordsCounts(baseIdx),
                lastWordsCounts(rightIdx)
            ),
        Nil
        ) - 1
    }

    protected[learnpl2] def findIdxWithMinCnt(counts: List[Int], alreadySelectedIndices: List[Int]): Int = {
        val countsWithIndices = counts.zipWithIndex.filter{case (c,i) => !alreadySelectedIndices.contains(i)}
        val minCnt = countsWithIndices.map(_._1).min
        val indicesWithMinCount = countsWithIndices.filter{case (c,i) => c == minCnt}.map(_._2)
        indicesWithMinCount(rnd.nextInt(indicesWithMinCount.length))
    }

    protected[learnpl2] def getRandomIndicesUnder50(elemsCnt: Int, pct: Int, lastWordsCounts: List[Int]): List[Int] = {
        //                pr("---------------------------------------------------------")
        //                pr(s"elemsCnt = $elemsCnt, pct = $pct")
        val resLength = List(math.round(elemsCnt * pct / 100.0).toInt).map(n => if (n == 0) 1 else n).apply(0)
        //                pr(s"resLength = $resLength")
        val step = List(math.round(elemsCnt.toDouble / resLength).toInt).map(n => if (n == 0) 1 else n).apply(0)
        //                pr(s"step = $step")
        val res = (2 to resLength).foldLeft(List(findIdxWithMinCnt(lastWordsCounts, Nil))) {(soFarRes, i) =>
            //            pr(s"soFarRes.head = ${soFarRes.head}")
            val baseIdx = (soFarRes.head + step) % elemsCnt
            //            pr(s"baseIdx = $baseIdx")

            def findNextIdx(baseIdx: Int): Int = {
                val res = (elemsCnt + baseIdx + calcShift(baseIdx, lastWordsCounts)) % elemsCnt
                if (!soFarRes.contains(res)) {
                    res
                } else {
                    findIdxWithMinCnt(lastWordsCounts, soFarRes)
                }
            }

            val nextIdx = findNextIdx(baseIdx)
            nextIdx::soFarRes
        }
        res
    }
}