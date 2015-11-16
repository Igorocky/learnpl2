package org.igye.learnpl2.models

import javafx.beans.property.SimpleStringProperty

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.RunInJfxThreadAndReturnResult

class LoadTextModel {
    implicit val log: Logger = LogManager.getLogger()
    private val text = new SimpleStringProperty("")

    def getText: String = {
        RunInJfxThreadAndReturnResult {
            text.getValue
        }
    }
    def setText(value: String): Unit = {
        RunInJfxThreadAndReturnResult {
            text.set(value)
        }
    }
    def getTextProperty: SimpleStringProperty = text
}
