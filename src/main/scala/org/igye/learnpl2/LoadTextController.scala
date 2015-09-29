package org.igye.learnpl2

import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import javafx.stage.Stage

class LoadTextController {
    var stage: Stage = _
    @FXML
    private var loadTextWindow: VBox = _
    def getLoadTextWindow = loadTextWindow

    @FXML
    private var textArea: TextArea = _
    def getTextArea = textArea

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
