package org.igye.learnpl2

import org.igye.learnpl2.enums.{Person, Gender, PartOfSpeech}

case class WordOld(writtenRepresentation: String,
                partOfSpeech: PartOfSpeech,
                number: Option[Number] = None,
                gender: Option[Gender] = None,
                person: Option[Person] = None
                   ) {
//    def this(writtenRepresentation: String, partOfSpeech: PartOfSpeech) = this(
//        writtenRepresentation,
//        partOfSpeech,
//        number = None,
//        gender = None,
//        person = None
//    )
}
