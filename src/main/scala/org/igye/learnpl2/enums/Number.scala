package org.igye.learnpl2.enums

import org.igye.learnpl2.enums.PartOfSpeech._
import org.igye.learnpl2.utils.Enum

case class Number(name: String)

object Number extends Enum[Number] {
    private def f(name: String) = field(Number(name))

    val SINGULAR = f("singular")
    val PLURAL = f("plural")
}
