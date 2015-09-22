package org.igye.learnpl2.enums

import org.igye.learnpl2.utils.Enum

case class Gender(name: String)

object Gender extends Enum[Gender] {
    private def f(name: String) = field(Gender(name))

    val MASCULINE = f("masculine")
    val FEMININE = f("feminine")
    val NEUTER = f("neuter")
}
