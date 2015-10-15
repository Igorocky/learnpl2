package org.igye.learnpl2.controllers

import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage

import org.igye.jfxutils.Initable
import org.igye.jfxutils.annotations.FxmlFile

@FxmlFile("fxml/LoadTextWindow.fxml")
class LoadTextController extends Initable {
    var stage: Stage = _
    @FXML
    protected var loadTextWindow: VBox = _
    def getLoadTextWindow = loadTextWindow

    @FXML
    protected var textArea: TextArea = _
    def getTextArea = textArea

    override def init(): Unit = {
        require(loadTextWindow != null)
        require(textArea != null)
    }

    var onLoadButtonPressed: EventHandler[ActionEvent] = _

    @FXML
    private def loadButtonPressed(event: ActionEvent): Unit = {
        onLoadButtonPressed.handle(event)
    }

    @FXML
    private def cancelButtonPressed(event: ActionEvent): Unit = {
        close()
    }

    def close(): Unit = {
        stage.close()
    }
}
