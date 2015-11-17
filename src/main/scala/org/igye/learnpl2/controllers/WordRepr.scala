package org.igye.learnpl2.controllers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.{Node, Group}
import javafx.scene.control.TextField
import javafx.scene.text.Text

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.properties.Trigger
import org.igye.jfxutils.{RunInJfxThreadForcibly, observableValueToObservableValueOperators, ChgListener}
import org.igye.learnpl2.models.Word

trait ParentHasWord {
    def getParent(): Node
    def getWord: Word = getParent().asInstanceOf[WordRepr].word
}

class WordRepr(val word: Word, val textElem: Text with ParentHasWord, val editElem: Option[TextField with ParentHasWord]) extends Group {
    implicit val log: Logger = LogManager.getLogger()

    val showTextField = new SimpleBooleanProperty(false)
    getChildren.add(textElem)
    showTextField ==> ChgListener{ chg=>
        getChildren.clear()
        if (chg.newValue) {
            getChildren.add(editElem.get)
        } else {
            getChildren.add(textElem)
        }
    }

    private var requestFocusTrigger: Option[Trigger] = None

    def setRequestFocusTrigger(trigger: Trigger) = {
        requestFocusTrigger = Some(trigger)
        if (editElem.isDefined) {
            trigger.action = () => RunInJfxThreadForcibly{
                editElem.get.requestFocus()
            }
        }
    }
}
