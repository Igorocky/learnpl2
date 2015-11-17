package org.igye.learnpl2.models

import javafx.beans.property.{BooleanProperty, StringProperty}

trait Word {
    val text: String
    val hiddable: Boolean
    val selected: BooleanProperty
    val mouseEntered: BooleanProperty
    val hidden: BooleanProperty
}
