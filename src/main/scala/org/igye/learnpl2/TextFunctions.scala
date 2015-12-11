package org.igye.learnpl2

import org.apache.logging.log4j.Logger

object TextFunctions {
    val sentenceDelimiter = """(?<=[\.!?])""".r
    def splitTextOnSentences(text: String): List[String] = {
        sentenceDelimiter.split(text).toList
    }

    val borderSymbol = """[\s\r\n,:;\."\(\)\[\]\\/!]"""
    val sentencePartsDelimiter = ("((?<=" + borderSymbol + ")(?!" + borderSymbol + "))|((?<!" + borderSymbol + ")(?=" + borderSymbol + "))").r
    def splitSentenceOnParts(sentence: String) = {
        sentencePartsDelimiter.split(sentence).toList
    }

    val hidablePattern = """^[\(\)-.,\s–":\[\]\\/;—!]+$""".r
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
}
