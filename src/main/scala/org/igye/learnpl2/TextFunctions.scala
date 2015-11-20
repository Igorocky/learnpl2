package org.igye.learnpl2

import org.apache.logging.log4j.Logger

object TextFunctions {
    val sentenceDelimiter = """(?<=\.)""".r
    def splitTextOnSentences(text: String): List[String] = {
        sentenceDelimiter.split(text).toList.map(_.trim)
    }

    val sentencePartsDelimiter = """(?<!(-|'|–))\b(?!(-|'|–)\w)""".r
    def splitSentenceOnParts(sentence: String) = {
        sentencePartsDelimiter.split(sentence).toList
    }

    val hidablePattern = """^[\(\)-.,\s–":\[\]\\/;]+$""".r
    def isHiddable(word: String): Boolean = {
        !hidablePattern.findFirstIn(word).isDefined
    }

    def checkUserInput(expectedText: String, userInput: String, log: Option[Logger]) = {
        val res = expectedText == userInput
        if (log.isDefined) {
            log.get.info(s"checkUserInput: expectedText = '$expectedText', userInput = '$userInput', checkUserInput.res = $res")
        }
        res
    }

    case class PathAndFilter(path: String, filter: String)
    val pathAndFilterPat = """(.*[\\/])?([^\\/]+)?""".r
    def extractPathAndFilter(input: String) = {
        val pathAndFilterPat(path, filter) = input
        PathAndFilter(if (path != null) path else "", if (filter != null) filter else "")
    }
}
