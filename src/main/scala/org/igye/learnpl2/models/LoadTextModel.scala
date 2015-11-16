package org.igye.learnpl2.models

import javafx.beans.property.SimpleStringProperty

trait LoadTextModel {
    def getText: String
    def setText(value: String): Unit
    def getTextProperty: SimpleStringProperty
}
