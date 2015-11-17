package org.igye.learnpl2.models

import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList

import org.igye.learnpl2.controllers.{ValidationStage, State}

trait MainWindowModel {
    val currState: ObjectProperty[State]
    val currValidationState: ObjectProperty[ValidationStage]
    def setText(text: String)
    val currSentence: ObservableList[Word]
    def selectWord(word: Word)
    def getSelectedWord: Option[Word]
    def next(): Unit
}
