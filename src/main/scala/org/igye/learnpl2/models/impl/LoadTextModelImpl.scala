package org.igye.learnpl2.models.impl

import java.io.File
import javafx.beans.property.{StringProperty, SimpleIntegerProperty, IntegerProperty, SimpleStringProperty}

import org.apache.commons.io.FileUtils
import org.igye.learnpl2.models.LoadTextModel

class LoadTextModelImpl extends LoadTextModel {
    private final val ENCODING = "UTF-8"

    val text = new SimpleStringProperty()
    override val loadedFrom: StringProperty = new SimpleStringProperty()
    override val caretPosition: IntegerProperty = new SimpleIntegerProperty()

    override def loadFromFile(filePath: String): Unit = {
        text.set(FileUtils.readFileToString(new File(filePath), ENCODING))
        loadedFrom.set(filePath)
    }

    override def saveAs(filePath: String): Unit = {
        loadedFrom.set(filePath)
        FileUtils.writeStringToFile(new File(filePath), text.get(), ENCODING)
    }

    override def save(): Unit = {
        saveAs(loadedFrom.get())
    }

    override def reset(): Unit = {
        text.set("")
        loadedFrom.set("")
    }
}
