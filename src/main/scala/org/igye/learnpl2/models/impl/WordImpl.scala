package org.igye.learnpl2.models.impl

import javafx.beans.property.{BooleanProperty, SimpleBooleanProperty}

import org.igye.learnpl2.models.Word

class WordImpl(override val text: String, override val hiddable: Boolean) extends Word {
    override val selected: BooleanProperty = new SimpleBooleanProperty(false)
    override val mouseEntered: BooleanProperty = new SimpleBooleanProperty(false)
    override val hidden: BooleanProperty = new SimpleBooleanProperty(this, "hidden", false)
}
