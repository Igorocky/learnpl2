package org.igye.learnpl2.models.impl

import javafx.beans.property.{BooleanProperty, ObjectProperty, SimpleBooleanProperty, SimpleObjectProperty}

import org.igye.learnpl2.models.Word

class WordImpl(override val text: String, override val hiddable: Boolean) extends Word {
    override val selected: BooleanProperty = new SimpleBooleanProperty(false)
    override val mouseEntered: BooleanProperty = new SimpleBooleanProperty(false)
    override val hidden: BooleanProperty = new SimpleBooleanProperty(this, "hidden", false)

    private var userInput: Option[String] = None
    override def setUserInput(userInput: String): Unit = {
        this.userInput = Some(userInput)
        userInputIsCorrect.set(None)
    }

    override def unsetUserInput(): Unit = {
        userInput = None
    }

    override def getUserInput: Option[String] = userInput

    override val awaitingUserInput: BooleanProperty = new SimpleBooleanProperty(this, "awaitingUserInput", false)
    override val userInputIsCorrect: ObjectProperty[Option[Boolean]] = new SimpleObjectProperty(None)
}
