package org.igye.learnpl2.models.impl

import javafx.beans.property.SimpleStringProperty

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.RunInJfxThreadAndReturnResult
import org.igye.learnpl2.models.LoadTextModel

class LoadTextModelImpl extends LoadTextModel {
    implicit val log: Logger = LogManager.getLogger()
    private val text = new SimpleStringProperty("")

    override def getText: String = {
        RunInJfxThreadAndReturnResult {
            text.getValue
        }
    }
    override def setText(value: String): Unit = {
        RunInJfxThreadAndReturnResult {
            text.set(value)
        }
    }
    override def getTextProperty: SimpleStringProperty = text
}
