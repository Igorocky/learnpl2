package org.igye.learnpl2.models.impl

import java.io.File
import javafx.beans.property.{StringProperty, SimpleIntegerProperty, IntegerProperty, SimpleStringProperty}

import org.apache.commons.io.FileUtils
import org.igye.learnpl2.models.LoadTextModel

class LoadTextModelImpl extends LoadTextModel {
    val text = new SimpleStringProperty()
    override val loadedFrom: StringProperty = new SimpleStringProperty()

    override def loadFromFile(filePath: String): Unit = {
        text.set(FileUtils.readFileToString(new File(filePath), "UTF-8"))
        loadedFrom.set(filePath)
    }

    override val caretPosition: IntegerProperty = new SimpleIntegerProperty()

    override def saveAs(filePath: String): Unit = {
        loadedFrom.set(filePath)
        FileUtils.writeStringToFile(new File(filePath), text.get(), "UTF-8")
    }

    override def save(): Unit = {
        saveAs(loadedFrom.get())
    }
}
