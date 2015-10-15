package org.igye.learnpl2.enums

import org.igye.commonutils.Enum

case class PartOfSpeech(name: String)

object PartOfSpeech extends Enum[PartOfSpeech] {
    private def f(name: String) = addElem(PartOfSpeech(name))

    val VERB = f("verb")
}
