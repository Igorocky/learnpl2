package org.igye.learnpl2

import java.util.Random

import scala.collection.mutable.ListBuffer

class Rnd {
    private val rnd = new Random()
    private val buf = ListBuffer[Int]()
    private var lastBound = -1

    def nextInt(bound: Int) = {
//        val pr = println(_:Any)
//        pr(s"buf.size = ${buf.size}, bound = $bound, lastBound = $lastBound")
        if (buf.isEmpty || lastBound != bound) {
            lastBound = bound
            buf ++= 0 until bound
            for (i <- 1 to bound*5) {
                buf += buf.remove(rnd.nextInt(buf.size))
            }
        }
        val res = buf.remove(rnd.nextInt(buf.size)) % bound
//        pr(s"nextInt.res = $res")
        res
    }
}