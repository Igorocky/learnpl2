package org.igye.learnpl2

import java.util.Random

import org.igye.learnpl2.models.Word

import scala.collection.mutable.ListBuffer

class RandomIndices {
    private val firstWordRnd = new Rnd()
    private val generalRnd = new Random()
    private var lastHash: Long = _
    private val lastWordsCounts = ListBuffer[Int]()

//    private val pr = println(_:Any)

    private def getRandomIndices(elemsCnt: Int, pct: Int): List[Int] = {
        if (elemsCnt != lastWordsCounts.length) {
            lastWordsCounts.clear()
            lastWordsCounts ++= (1 to elemsCnt).map(i => 1)
        }
//                pr("---------------------------------------------------------")
//                pr(s"elemsCnt = $elemsCnt, pct = $pct")
        val resLength = List(math.round(elemsCnt * pct / 100.0).toInt).map(n => if (n == 0) 1 else n).apply(0)
//                pr(s"resLength = $resLength")
        val step = List(math.round(elemsCnt.toDouble / resLength).toInt).map(n => if (n == 0) 1 else n).apply(0)
//                pr(s"step = $step")
        def addToRes(idx: Int, soFarRes: List[Int]): List[Int] = {
            lastWordsCounts.update(idx, lastWordsCounts(idx) + 1)
            idx::soFarRes
        }
        val res = (2 to resLength).foldLeft(addToRes(firstWordRnd.nextInt(elemsCnt), Nil)) {(res, step) =>
            val baseIdx = (res.head + step) % elemsCnt
            addToRes(
                (elemsCnt + baseIdx + calcShift(baseIdx)) % elemsCnt,
                res
            )
        }
        firstWordRnd.removeFromBuffer(res)
//                pr(s"getRandomIndices.res = $res")
        res
    }

    private def calcShift(baseIdx: Int): Int = {
        val leftIdx = if (baseIdx > 0) baseIdx - 1 else lastWordsCounts.length - 1
        val rightIdx = if (baseIdx < lastWordsCounts.length - 1) baseIdx + 1 else 0
        val sumCnt = (lastWordsCounts(leftIdx) + lastWordsCounts(baseIdx) + lastWordsCounts(rightIdx)).toDouble
        val leftShiftPct = (1 - lastWordsCounts(leftIdx) / sumCnt) / 2
        val rightShiftPct = 1 - (1 - lastWordsCounts(rightIdx) / sumCnt) / 2
        val shiftPot = generalRnd.nextDouble()
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

    def findSuitableIndices(words: List[Word], pct: Int): List[Int] = {
        if (1 == 1 || generalRnd.nextInt(3) > 1) {
            getRandomIndices(words.length, pct)
        } else {
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
    }
}
