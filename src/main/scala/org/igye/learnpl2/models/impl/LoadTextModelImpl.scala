package org.igye.learnpl2.models.impl

import java.io.File
import javafx.beans.property.SimpleStringProperty

import org.apache.commons.io.FileUtils
import org.igye.learnpl2.models.LoadTextModel

class LoadTextModelImpl extends LoadTextModel {
    val text = new SimpleStringProperty()

    override def loadFromFile(filePath: String): Unit = {
        text.set(FileUtils.readFileToString(new File(filePath), "UTF-8"))
    }
}
