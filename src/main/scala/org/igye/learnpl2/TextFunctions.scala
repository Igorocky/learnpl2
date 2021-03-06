package org.igye.learnpl2

import org.apache.logging.log4j.Logger

object TextFunctions {
    /*
       '—' - 2014
       '…' - 2026
       '„' - 201E
       '”' - 201D
      */

    val sentenceDelimiter = """(?<=[\.!?\u2026]+)(?![\.!?\u2026]+)""".r
    def splitTextOnSentences(text: String): List[String] = {
        sentenceDelimiter.split(text).toList
    }

    val borderSymbol = """[\s\r\n,:;\."\(\)\[\]\\/!?\*\u2026\u201E\u201D]"""
    val sentencePartsDelimiter = ("((?<=" + borderSymbol + ")(?!" + borderSymbol + "))|((?<!" + borderSymbol + ")(?=" + borderSymbol + "))").r
    def splitSentenceOnParts(sentence: String) = {
        sentencePartsDelimiter.split(sentence).toList
    }

    val hidablePattern = """^[\(\)-.,\s–":\[\]\\/;!?\u2014\u2026\u201E\u201D]+$""".r
    def isHiddable(word: String): Boolean = {
        !hidablePattern.findFirstIn(word).isDefined
    }

    def checkUserInput(expectedText: String, userInput: String, log: Option[Logger]) = {
        val res = expectedText.trim == userInput.trim
        if (log.isDefined) {
            log.get.info(s"checkUserInput: expectedText = '$expectedText', userInput = '$userInput', checkUserInput.res = $res")
        }
        res
    }
}
