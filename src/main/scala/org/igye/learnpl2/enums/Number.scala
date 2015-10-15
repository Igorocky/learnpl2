package org.igye.learnpl2.enums

import org.igye.commonutils.Enum

case class Number(name: String)

object Number extends Enum[Number] {
    private def f(name: String) = addElem(Number(name))

    val SINGULAR = f("singular")
    val PLURAL = f("plural")
}
