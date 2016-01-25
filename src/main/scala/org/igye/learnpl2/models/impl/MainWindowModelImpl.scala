package org.igye.learnpl2.models.impl

import javafx.beans.property.{ObjectProperty, SimpleIntegerProperty, SimpleObjectProperty}
import javafx.collections.FXCollections

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.observableValueToObservableValueOperators
import org.igye.jfxutils.properties.ChgListener
import org.igye.learnpl2.TextFunctions.{splitSentenceOnParts, splitTextOnSentences}
import org.igye.learnpl2.controllers.State
import org.igye.learnpl2.controllers.State._
import org.igye.learnpl2.models.{MainWindowModel, Word}
import org.igye.learnpl2.settings.Settings
import org.igye.learnpl2.{Rnd, RandomIndices, TextFunctions}

import scala.collection.JavaConversions._

class MainWindowModelImpl extends MainWindowModel {
    private val log: Logger = LogManager.getLogger()
    private val spellCheckerLog = Some(LogManager.getLogger("spellChecker"))

    override val currState: ObjectProperty[State] = new SimpleObjectProperty(NOT_LOADED)

    override val minMax: SimpleObjectProperty[(Int, Int)] = new SimpleObjectProperty((0, 0))

    private var text: Option[List[List[WordImpl]]] = None
    val currSentenceIdx = new SimpleIntegerProperty(-1)
    def sentenceCount: Int = text.map(_.length).getOrElse(0)

    private var randomOrderOfSentences = false
    private val rndForSentenceIndex = new Rnd
    private var skipReadingStage = false

    override val currSentence = FXCollections.observableArrayList[Word]()

    private val rndIndices = new RandomIndices

    override val selectedWord: ObjectProperty[Option[Word]] = new SimpleObjectProperty[Option[Word]](None)
    currSentenceIdx ==> ChgListener{chg=>
        selectedWord.set(None)
    }
    currState ==> ChgListener{chg=>
        selectedWord.set(None)
    }

    override def setText(text: String, caretPosition: Int): Unit = {
        this.text = Some(parseText(text))
        rndForSentenceIndex.refresh()
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
                selectWord(w)
            }
        }
    }

    override def selectionRange: (Int, Int) = {
        var res = (0, 0)
        val selectedWord = getWordUnderFocus.orElse(this.selectedWord.get)
        if (selectedWord.isDefined) {
            traverseAllWords{(s,l,r,w) =>
                if (w == selectedWord.get) {
                    res = (l, r)
                }
            }
        } else if (currSentence.nonEmpty) {
            val sPos = getStartPositionOfWordInCurrSentence
            res = (sPos, sPos)
        }
        res
    }

    private def getStartPositionOfWordInCurrSentence: Int = {
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
        splitTextOnSentences(text).map{sentence =>
            splitSentenceOnParts(sentence).foldLeft((List[WordImpl](), false)){
                case ((soFarRes, inComment), wordText) =>
                    val trimmedWordText = wordText.trim
                    val (isHiddable, inCommentNew) = if (trimmedWordText.contains("/*")) {
                        (false, true)
                    } else if (trimmedWordText.contains("*/")) {
                        (false, false)
                    } else {
                        (!inComment && TextFunctions.isHiddable(wordText), inComment)
                    }
                    (soFarRes:::(new WordImpl(wordText, isHiddable))::Nil, inCommentNew)
            }._1
        }
    }

    private def resetWord(word: Word): Unit = {
        word.hidden.set(false)
        word.awaitingUserInput.set(false)
        word.userInputIsCorrect.set(None)
        word.unsetUserInput()
        word.selected.set(false)
    }

    def goToSentence(sentenceIdx: Int): Unit = {
        if (text.isDefined && sentenceIdx >= 0 && sentenceIdx < text.get.length) {
            currSentence.clear()
            currSentence.addAll(text.get(sentenceIdx))
            currSentenceIdx.set(sentenceIdx)
            currSentence.foreach(resetWord)
            currState.set(ONLY_TEXT)
            if (skipReadingStage) {
                next()
            }
        }
    }

    override def selectWord(word: Word): Unit = {
        currSentence.find(_.selected.get).foreach(_.selected.set(false))
        word.selected.set(true)
        if (word.hidden.get && !word.awaitingUserInput.get) {
            focusWord(word)
        }
        selectedWord.set(Some(word))
    }

    def unselectWord(word: Word): Unit = {
        word.selected.set(false)
        selectedWord.set(None)
    }

    override def getWordUnderFocus: Option[Word] = {
        currSentence.find(_.awaitingUserInput.get)
    }

    override def setRandomOrderOfSentences(random: Boolean): Unit = {
        randomOrderOfSentences = random
    }

    override def setSkipReadingStage(skipReadingStage: Boolean): Unit = {
        this.skipReadingStage = skipReadingStage
    }

    override def next(): Unit = {
        if (currState.get() == ONLY_TEXT) {
            hideWordsOfCurrentSentence()
            currState.set(TEXT_WITH_INPUTS)
        } else if (currState.get() == TEXT_WITH_INPUTS) {
            nextSentence()
        }
    }

    private def hideWordsOfCurrentSentence(): Unit = {
        currSentence.foreach(resetWord)
        val hidableWords = currSentence.filter(_.hiddable).toList
        rndIndices.getRandomIndices(
            hidableWords.length,
            Settings.probabilityPercent,
            text.get.apply(currSentenceIdx.get).hashCode()
        ).foreach(hidableWords(_).hidden.set(true))
        minMax.set(rndIndices.getMinMax)
        currSentence.find(_.hidden.get()).foreach(_.awaitingUserInput.set(true))
    }

    override def refreshHiddenWords(): Unit = {
        if (currState.get() == TEXT_WITH_INPUTS) {
            hideWordsOfCurrentSentence()
        }
    }

    override def nextSentence(): Unit = {
        if (currState.get() != NOT_LOADED) {
            if (randomOrderOfSentences) {
                currSentenceIdx.set(rndForSentenceIndex.nextInt(text.get.size))
                goToSentence(currSentenceIdx.get())
            } else {
                if (currSentenceIdx.get() < text.get.size - 1) {
                    currSentenceIdx.set(currSentenceIdx.get() + 1)
                    goToSentence(currSentenceIdx.get())
                } else {
                    gotoNotLoadedState()
                }
            }
        }
    }

    override def back(): Unit = {
        if (currState.get() == TEXT_WITH_INPUTS) {
            goToSentence(currSentenceIdx.get())
        } else if (currState.get() == ONLY_TEXT) {
            if (currSentenceIdx.get() > 0) {
                currSentenceIdx.set(currSentenceIdx.get() - 1)
                goToSentence(currSentenceIdx.get())
            } else {
                gotoNotLoadedState()
            }
        }
    }

    private def gotoNotLoadedState(): Unit = {
        currSentence.clear()
        text = None
        currState.set(NOT_LOADED)
        currSentenceIdx.set(-1)
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
                val thereWereUncheckedWordsExceptCurrent = hiddenWords
                    .filter(_ != curWord.getOrElse(null))
                    .find(_.userInputIsCorrect.get().isEmpty).isDefined
                val thereWereUncheckedWords = hiddenWords
                    .find(_.userInputIsCorrect.get().isEmpty).isDefined
                hiddenWords.filter(_.userInputIsCorrect.get().isEmpty).foreach(w=>
                    w.userInputIsCorrect.set(Some(TextFunctions.checkUserInput(w.text, w.getUserInput.get, spellCheckerLog)))
                )
                val firstIncorrectWord = (if (thereWereUncheckedWordsExceptCurrent) None else curWord)
                    .flatMap(cw => hiddenWords.dropWhile(_ != curWord.get).find(!_.userInputIsCorrect.get().get))
                    .orElse(hiddenWords.find(!_.userInputIsCorrect.get().get))
                if (firstIncorrectWord.isDefined) {
                    firstIncorrectWord.get.awaitingUserInput.set(true)
                } else if (!thereWereUncheckedWords) {
                    next()
                }
            }
        }
    }

    override def selectNextWord(step: Int): Unit = {
        val selectedWordOpt = selectedWord.get
        var idx = if (selectedWordOpt.isDefined) {
            unselectWord(selectedWordOpt.get)
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
            idx == -1 || currSentence.get(idx).hiddable
        }

        incSelectedWordIdx()
        while(!isIdxAppropriate()) {
            incSelectedWordIdx()
        }
        if (idx != -1) {
            selectWord(currSentence.get(idx))
        }
    }

    override def focusWord(word: Word): Unit = {
        currSentence.foreach(_.awaitingUserInput.set(false))
        word.awaitingUserInput.set(true)
        selectWord(word)
    }

    override def resetCounters(): Unit = {
        rndIndices.resetCounters()
        minMax.set((0, 0))
    }
}
