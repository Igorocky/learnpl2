package org.igye.learnpl2.enums

import org.igye.commonutils.Enum

case class Person(name: String)

object Person extends Enum[Person] {
    private def f(name: String) = addElem(Person(name))

    val FIRST = f("1st")
    val SECOND = f("2nd")
    val THIRD = f("3rd")
}
