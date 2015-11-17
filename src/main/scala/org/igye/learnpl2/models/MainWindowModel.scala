package org.igye.learnpl2.models

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList

import org.igye.learnpl2.controllers.State

trait MainWindowModel {
    val currState: ObjectProperty[State]
    def setText(text: String)
    val currSentence: ObservableList[Word]
    def selectWord(word: Word)
    def getSelectedWord: Option[Word]
    def next(): Unit
    def back(): Unit
    def gotoNextWordToBeEnteredOrSwitchToNextSentence()
}
