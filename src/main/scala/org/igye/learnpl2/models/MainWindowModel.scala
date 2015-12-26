package org.igye.learnpl2.models

import javafx.beans.property.{IntegerProperty, ObjectProperty}
import javafx.collections.ObservableList

import org.igye.learnpl2.controllers.State

trait MainWindowModel {
    val currState: ObjectProperty[State]
    def setText(text: String, caretPosition: Int)
    def selectionRange: (Int, Int)
    val currSentence: ObservableList[Word]
    val currSentenceIdx: IntegerProperty
    def sentenceCount: Int
    def goToSentence(sentenceIdx: Int): Unit
    def selectWord(word: Word)
    def getSelectedWord: Option[Word]
    def getWordUnderFocus: Option[Word]
    def next(): Unit
    def nextSentence(): Unit
    def back(): Unit
    def gotoNextWordToBeEnteredOrSwitchToNextSentence()
    def selectNextWord(step: Int): Unit
    def focusWord(word: Word)
}
