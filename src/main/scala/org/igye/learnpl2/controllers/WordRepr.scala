package org.igye.learnpl2.controllers

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.{Node, Group}
import javafx.scene.control.TextField
import javafx.scene.text.Text

import org.igye.jfxutils.{observableValueToObservableValueOperators, ChgListener}
import org.igye.learnpl2.models.Word

trait ParentHasWord {
    def getParent(): Node
    def getWord: Word = getParent().asInstanceOf[WordRepr].word
}

class WordRepr(val word: Word, val textElem: Text with ParentHasWord, val editElem: Option[TextField with ParentHasWord]) extends Group {
    val showTextEdit = new SimpleBooleanProperty(false)
    getChildren.add(textElem)
    showTextEdit ==> ChgListener{ chg=>
        getChildren.clear()
        if (chg.newValue) {
            getChildren.add(editElem.get)
        } else {
            getChildren.add(textElem)
        }
    }
}
