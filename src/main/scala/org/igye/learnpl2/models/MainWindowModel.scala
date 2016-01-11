package org.igye.learnpl2.models

import javafx.beans.property.{IntegerProperty, ObjectProperty}
import javafx.collections.ObservableList

import org.igye.learnpl2.controllers.State

trait MainWindowModel {
    val currState: ObjectProperty[State]
    val currSentence: ObservableList[Word]
    val currSentenceIdx: IntegerProperty
    val selectedWord: ObjectProperty[Option[Word]]
    val minMax: ObjectProperty[(Int, Int)]

    def setText(text: String, caretPosition: Int)
    def selectionRange: (Int, Int)
    def sentenceCount: Int
    def goToSentence(sentenceIdx: Int): Unit
    def selectWord(word: Word)
    def getWordUnderFocus: Option[Word]
    def next(): Unit
    def nextSentence(): Unit
    def back(): Unit
    def gotoNextWordToBeEnteredOrSwitchToNextSentence()
    def selectNextWord(step: Int): Unit
    def focusWord(word: Word)
    def resetCounters()
}
