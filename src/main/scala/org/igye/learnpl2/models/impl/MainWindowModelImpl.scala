package org.igye.learnpl2.models.impl

import java.util.Random
import javafx.beans.property.{SimpleObjectProperty, ObjectProperty}
import javafx.collections.{FXCollections, ObservableList}

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.controllers.{ValidationStage, State}
import org.igye.learnpl2.controllers.State._
import org.igye.learnpl2.controllers.ValidationStage._
import org.igye.learnpl2.models.{MainWindowModel, Word}

import scala.collection.JavaConversions._

class MainWindowModelImpl extends MainWindowModel {
    implicit val log: Logger = LogManager.getLogger()

    override val currState: ObjectProperty[State] = new SimpleObjectProperty(NOT_LOADED)
    override val currValidationState: ObjectProperty[ValidationStage] = new SimpleObjectProperty(FILL_INPUTS)

    private var text: Option[List[List[String]]] = None
    private var currSentenceIdx = -1

    override val currSentence: ObservableList[Word] = FXCollections.observableArrayList[Word]()

    private val random = new Random()

    override def setText(text: String): Unit = {
        this.text = Some(parseText(text))
        currSentenceIdx = 0
        updateCurrSentence()
        currState.set(ONLY_TEXT)
    }

    private def parseText(text: String): List[List[String]] = {
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts)
    }

    private def updateCurrSentence(): Unit = {
        currSentence.clear()
        text.get.get(currSentenceIdx).foreach{wordText =>
            currSentence.add(new WordImpl(wordText, TextFunctions.isHiddable(wordText)))
        }
    }

    override def selectWord(word: Word): Unit = {
        currSentence.find(_.selected.get).foreach(_.selected.set(false))
        word.selected.set(true)
    }

    override def getSelectedWord: Option[Word] = {
        currSentence.find(_.selected.get)
    }

    override def next(): Unit = {
        if (currState.get() == ONLY_TEXT) {
            currSentence.foreach(w => if (w.hiddable && random.nextInt(100) < 10) w.hidden.set(true))
            currState.set(TEXT_WITH_INPUTS)
            currValidationState.set(FILL_INPUTS)
            if (currSentence.find(_.hidden.get).isEmpty) {
                next()
            }
        } else if (currState.get() == TEXT_WITH_INPUTS) {
            if (currSentenceIdx < text.get.size - 1) {
                currSentenceIdx += 1
                updateCurrSentence()
                currState.set(ONLY_TEXT)
            } else {
                currSentence.clear()
                text = None
                currState.set(NOT_LOADED)
            }
        }
    }
}
