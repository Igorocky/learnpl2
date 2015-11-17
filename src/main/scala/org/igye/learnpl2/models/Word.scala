package org.igye.learnpl2.models

import javafx.beans.property.{BooleanProperty, ObjectProperty}

trait Word {
    val text: String
    val hiddable: Boolean
    val selected: BooleanProperty
    val mouseEntered: BooleanProperty
    val hidden: BooleanProperty
    def setUserInput(userInput: String)
    def getUserInput: Option[String]
    val awaitingUserInput: BooleanProperty
    val userInputIsCorrect: ObjectProperty[Option[Boolean]]
}
