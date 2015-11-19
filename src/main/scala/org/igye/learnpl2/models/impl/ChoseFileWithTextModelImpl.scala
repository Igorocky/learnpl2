package org.igye.learnpl2.models.impl

import javafx.beans.property.{SimpleStringProperty, StringProperty}

import org.igye.learnpl2.models.ChoseFileWithTextModel

class ChoseFileWithTextModelImpl extends ChoseFileWithTextModel {
    override val filePath: StringProperty = new SimpleStringProperty()
}
