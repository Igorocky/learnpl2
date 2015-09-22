package org.igye.learnpl2.enums

import org.igye.learnpl2.utils.Enum

case class PartOfSpeech(name: String)

object PartOfSpeech extends Enum[PartOfSpeech] {
    private def f(name: String) = field(PartOfSpeech(name))

    val VERB = f("verb")
}
