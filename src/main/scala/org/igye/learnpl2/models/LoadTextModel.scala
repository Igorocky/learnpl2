package org.igye.learnpl2.models

import javafx.beans.property.{IntegerProperty, StringProperty}

trait LoadTextModel {
    val text: StringProperty
    def loadFromFile(filePath: String)
    def saveAs(filePath: String)
    def save()
    val caretPosition: IntegerProperty
    val loadedFrom: StringProperty
}
