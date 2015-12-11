package org.igye.learnpl2.models.impl

import java.util.Random
import javafx.beans.property.{SimpleIntegerProperty, ObjectProperty, SimpleObjectProperty}
import javafx.collections.FXCollections

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.controllers.State
import org.igye.learnpl2.controllers.State._
import org.igye.learnpl2.models.{MainWindowModel, Word}
import org.igye.learnpl2.settings.Settings

import scala.collection.JavaConversions._

class MainWindowModelImpl extends MainWindowModel {
    private val log: Logger = LogManager.getLogger()
    private val spellCheckerLog = Some(LogManager.getLogger("spellChecker"))

    override val currState: ObjectProperty[State] = new SimpleObjectProperty(NOT_LOADED)

    private var text: Option[List[List[WordImpl]]] = None
    val currSentenceIdx = new SimpleIntegerProperty(-1)
    def sentenceCount: Int = text.map(_.length).getOrElse(0)

    override val currSentence = FXCollections.observableArrayList[Word]()

    private val random = new Random()

    override def setText(text: String, caretPosition: Int): Unit = {
        this.text = Some(parseText(text))
        if (caretPosition > 0) {
            selectWordByCaretPosition(caretPosition)
        }
        currSentenceIdx.set(getSentenceWithCaretIdxOrZero(caretPosition))
        goToSentence(currSentenceIdx.get())
    }

    private def getSentenceWithCaretIdxOrZero(caretPosition: Int): Int = {
        var res = 0
        traverseAllWords{(s,l,r,w) =>
            if (l <= caretPosition && caretPosition <= r) {
                res = s
            }
        }
        res
    }

    private def traverseAllWords(consumer: (Int/*sentence index*/, Int/*leftPosition*/, Int/*rightPosition*/, Word) => Unit): Unit = {
        if (text.isDefined) {
            var leftPosition = 0
            var rightPosition = 0
            for (s <- 0 until text.get.length) {
                for (w <- 0 until text.get(s).length) {
                    val word = text.get(s)(w)
                    rightPosition += word.text.replaceAllLiterally("\r\n", "\n").length
                    consumer(s, leftPosition, rightPosition, word)
                    leftPosition = rightPosition
                }
            }
        }
    }

    private def selectWordByCaretPosition(caretPosition: Int): Unit = {
        traverseAllWords{(s, l, r, w)=>
            if (w.hiddable && l <= caretPosition && caretPosition <= r) {
                traverseAllWords((s, l, r, w) => w.selected.set(false))
                w.selected.set(true)
            }
        }
    }

    override def caretPosition: Int = {
        var res = 0
        val selectedWord = getSelectedWord
        if (selectedWord.isDefined) {
            traverseAllWords((s,l,r,w) => if (w == selectedWord.get) {res = (l + r)/2})
        } else if (currSentence.nonEmpty) {
            res = getStartPositionOfWordInCurrSentence
        }
        res
    }

    private def getStartPositionOfWordInCurrSentence = {
        var res = 0
        if (currSentence.nonEmpty) {
            var firstNonemptyWordWasFound = false
            traverseAllWords{(s, l, r, w)=>
                if (currSentence.contains(w) && !firstNonemptyWordWasFound) {
                    res = l
                    if (StringUtils.replaceChars(w.text.trim, "\r\n", "").nonEmpty) {
                        firstNonemptyWordWasFound = true
                    }
                }
            }
        }
        res
    }

    private def parseText(text: String): List[List[WordImpl]] = {
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts(_).map{wordText =>
            new WordImpl(wordText, TextFunctions.isHiddable(wordText))
        })
    }

    def goToSentence(sentenceIdx: Int): Unit = {
        if (text.isDefined && sentenceIdx >= 0 && sentenceIdx < text.get.length) {
            currSentence.clear()
            currSentence.addAll(text.get(sentenceIdx))
            currSentenceIdx.set(sentenceIdx)
            currSentence.foreach(_.hidden.set(false))
            currState.set(ONLY_TEXT)
        }
    }

    override def selectWord(word: Word): Unit = {
        currSentence.find(_.selected.get).foreach(_.selected.set(false))
        word.selected.set(true)
    }

    override def getSelectedWord: Option[Word] = {
        currSentence.find(_.selected.get)
    }

    override def next(): Unit = {
        if (currState.get() == ONLY_TEXT) {
            currSentence.foreach(w => if (w.hiddable && random.nextInt(100) < Settings.probabilityPercent) w.hidden.set(true))
            currSentence.find(_.hidden.get()).foreach(_.awaitingUserInput.set(true))
            currState.set(TEXT_WITH_INPUTS)
            if (currSentence.find(_.hidden.get).isEmpty) {
                next()
            }
        } else if (currState.get() == TEXT_WITH_INPUTS) {
            if (currSentenceIdx.get() < text.get.size - 1) {
                currSentenceIdx.set(currSentenceIdx.get() + 1)
                goToSentence(currSentenceIdx.get())
            } else {
                currSentence.clear()
                text = None
                currState.set(NOT_LOADED)
                currSentenceIdx.set(-1)
            }
        }
    }

    override def back(): Unit = {
        if (currState.get() == TEXT_WITH_INPUTS) {
            currSentence.foreach(_.hidden.set(false))
            currState.set(ONLY_TEXT)
        } else if (currState.get() == ONLY_TEXT) {
            if (currSentenceIdx.get() > 0) {
                currSentenceIdx.set(currSentenceIdx.get() - 1)
                goToSentence(currSentenceIdx.get())
            } else {
                currSentence.clear()
                text = None
                currState.set(NOT_LOADED)
                currSentenceIdx.set(-1)
            }
        }
    }

    override def gotoNextWordToBeEnteredOrSwitchToNextSentence(): Unit = {
        if (currState.get() != NOT_LOADED) {
            val curWord = currSentence.find(_.awaitingUserInput.get)
            currSentence.foreach(_.awaitingUserInput.set(false))
            val hiddenWords = currSentence.filter(_.hidden.get())
            val firstWordWithoutUserInput = curWord
                .flatMap(cw => hiddenWords.dropWhile(_ != curWord.get).find(_.getUserInput.isEmpty))
                .orElse(hiddenWords.find(_.getUserInput.isEmpty))
            if (firstWordWithoutUserInput.isDefined) {
                firstWordWithoutUserInput.get.awaitingUserInput.set(true)
            } else {
                hiddenWords.filter(_.userInputIsCorrect.get().isEmpty).foreach(w=>
                    w.userInputIsCorrect.set(Some(TextFunctions.checkUserInput(w.text, w.getUserInput.get, spellCheckerLog)))
                )
                val firstIncorrectWord = curWord
                    .flatMap(cw => hiddenWords.dropWhile(_ != curWord.get).find(!_.userInputIsCorrect.get().get))
                    .orElse(hiddenWords.find(!_.userInputIsCorrect.get().get))
                if (firstIncorrectWord.isDefined) {
                    firstIncorrectWord.get.awaitingUserInput.set(true)
                } else {
                    next()
                }
            }
        }
    }

    override def selectNextWord(step: Int): Unit = {
        val selectedWordOpt = currSentence.find(_.selected.get())
        var idx = if (selectedWordOpt.isDefined) {
            selectedWordOpt.get.selected.set(false)
            currSentence.indexOf(selectedWordOpt.get)
        } else {
            -1
        }

        def incSelectedWordIdx() = {
            idx += step
            if (idx > currSentence.length - 1) {
                idx = -1
            } else if (idx < -1) {
                idx = currSentence.length - 1
            }
        }
        def isIdxAppropriate() = {
            idx == -1 || currSentence.get(idx).hiddable && !currSentence.get(idx).awaitingUserInput.get()
        }

        incSelectedWordIdx()
        while(!isIdxAppropriate()) {
            incSelectedWordIdx()
        }
        if (idx != -1) {
            currSentence.get(idx).selected.set(true)
        }
    }

    override def focusWord(word: Word): Unit = {
        currSentence.foreach(_.awaitingUserInput.set(false))
        word.awaitingUserInput.set(true)
    }
}
