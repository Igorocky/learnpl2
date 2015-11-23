package org.igye.learnpl2.models

import javafx.beans.property.{IntegerProperty, StringProperty}

trait LoadTextModel {
    val text: StringProperty
    def loadFromFile(filePath: String)
    val caretPosition: IntegerProperty
}
