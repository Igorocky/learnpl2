package org.igye.learnpl2.models.impl

import java.util.Random
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.collections.FXCollections

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

    private var text: Option[List[List[String]]] = None
    private var currSentenceIdx = -1

    override val currSentence = FXCollections.observableArrayList[Word]()

    private val random = new Random()

    override def setText(text: String): Unit = {
        this.text = Some(parseText(text))
        currSentenceIdx = 0
        updateCurrSentence()
        currState.set(ONLY_TEXT)
    }

    private def parseText(text: String): List[List[String]] = {
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts)
    }

    private def updateCurrSentence(): Unit = {
        currSentence.clear()
        text.get.get(currSentenceIdx).foreach{wordText =>
            currSentence.add(new WordImpl(wordText, TextFunctions.isHiddable(wordText)))
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
            if (currSentenceIdx < text.get.size - 1) {
                currSentenceIdx += 1
                updateCurrSentence()
                currState.set(ONLY_TEXT)
            } else {
                currSentence.clear()
                text = None
                currState.set(NOT_LOADED)
            }
        }
    }

    override def back(): Unit = {
        if (currState.get() == TEXT_WITH_INPUTS) {
            currSentence.foreach(_.hidden.set(false))
            currState.set(ONLY_TEXT)
        } else if (currState.get() == ONLY_TEXT) {
            if (currSentenceIdx > 0) {
                currSentenceIdx -= 1
                updateCurrSentence()
            } else {
                currSentence.clear()
                text = None
                currState.set(NOT_LOADED)
            }
        }
    }

    override def gotoNextWordToBeEnteredOrSwitchToNextSentence(): Unit = {
        if (currState.get() != NOT_LOADED) {
            currSentence.foreach(_.awaitingUserInput.set(false))
            val hiddenWords = currSentence.filter(_.hidden.get())
            val firstWordWithoutUserInput = hiddenWords.find(_.getUserInput.isEmpty)
            if (firstWordWithoutUserInput.isDefined) {
                firstWordWithoutUserInput.get.awaitingUserInput.set(true)
            } else {
                hiddenWords.filter(_.userInputIsCorrect.get().isEmpty).foreach(w=>
                    w.userInputIsCorrect.set(Some(TextFunctions.checkUserInput(w.text, w.getUserInput.get, spellCheckerLog)))
                )
                val firstIncorrectWord = hiddenWords.find(!_.userInputIsCorrect.get().get)
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
}
