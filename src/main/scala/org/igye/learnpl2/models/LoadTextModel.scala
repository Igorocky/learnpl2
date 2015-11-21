package org.igye.learnpl2.models

import javafx.beans.property.StringProperty

trait LoadTextModel {
    val text: StringProperty
    def loadFromFile(filePath: String)
}
