package org.igye.learnpl2.enums

import org.igye.commonutils.Enum

case class Gender(name: String)

object Gender extends Enum[Gender] {
    private def f(name: String) = addElem(Gender(name))

    val MASCULINE = f("masculine")
    val FEMININE = f("feminine")
    val NEUTER = f("neuter")
}
