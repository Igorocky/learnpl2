package org.igye.learnpl2

import java.util.Random

class Rnd {
    private val rnd = new Random()

    def nextInt(bound: Int) = {
        for (i <- 1 to 17) rnd.nextInt(bound)
        rnd.nextInt(bound)
    }
}