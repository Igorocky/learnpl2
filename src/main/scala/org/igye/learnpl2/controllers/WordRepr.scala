package org.igye.learnpl2.controllers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.Pane

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{observableValueToObservableValueOperators, parentToParentOps, propertyToPropertyOperators}
import org.igye.jfxutils.concurrency.RunInJfxThreadForcibly
import org.igye.jfxutils.properties.{ChgListener, Trigger}
import org.igye.learnpl2.models.Word

trait ParentHasWord {
    def getParent(): Node
    def getWord: Word = getParent().asInstanceOf[WordRepr].word
}

class WordRepr(val word: Word, val textElem: Label with ParentHasWord, val editElem: Option[TextField with ParentHasWord]) extends Pane {
    implicit val log: Logger = LogManager.getLogger()

    val showTextField = new SimpleBooleanProperty(false)
    showTextField ==> ChgListener(chg => setContent())

    this.requestLayoutOnChangeOf(prefWidthProperty())
    minWidthProperty() <== prefWidthProperty()
    maxWidthProperty() <== prefWidthProperty()
    this.requestLayoutOnChangeOf(prefHeightProperty())
    minHeightProperty() <== prefHeightProperty()
    maxHeightProperty() <== prefHeightProperty()

    setContent()

    private def setContent(): Unit = {
        getChildren.clear()
        if (showTextField.get()) {
            editElem.get.setText("")
            getChildren.add(editElem.get)
            prefWidthProperty() <== editElem.get.widthProperty()
            prefHeightProperty() <== editElem.get.heightProperty()
        } else {
            getChildren.add(textElem)
            prefWidthProperty() <== textElem.widthProperty()
            prefHeightProperty() <== textElem.heightProperty()
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
