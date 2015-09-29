package org.igye.learnpl2

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}

import org.igye.commonutils.FutureLoggable
import org.igye.jfxutils._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

class MainWindowController {
    implicit val log = LoggerFactory.getLogger(this.getClass)

    var primaryStage: Stage = _

    @FXML
    var mainWindow: Parent = _
    @FXML
    var contentPane: Pane = _

    @FXML
    def initialize(): Unit = {
        require(mainWindow != null)
        require(contentPane != null)
    }

    @volatile
    private var text: List[List[String]] = _
    private var currSentenceIdx: Int = _

    @FXML
    private def loadTextButtonPressed(event: ActionEvent): Unit = {
        val loadTextWnd = FxmlSupport.load[LoadTextController]("fxml/LoadTextWindow.fxml")
        val loadTextStage = new Stage()
        loadTextWnd.stage = loadTextStage
        loadTextStage.setScene(new Scene(loadTextWnd.getLoadTextWindow))
        loadTextStage.initOwner(primaryStage)
        loadTextStage.initModality(Modality.WINDOW_MODAL)

        loadTextWnd.onLoadButtonPressed = JfxActionEventHandler {e =>
            FutureLoggable {
                text = parseText(loadTextWnd.getTextArea.getText)
                currSentenceIdx = 0
                RunInJfxThread {
                    loadTextWnd.close()
                }
                showOnlyText()
            }
        }

        loadTextStage.show()
    }

    private def parseText(text: String): List[List[String]] = {
        text.split("\\.\\s").map(_.split("\\s").toList).toList
    }

    private def showOnlyText(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            val textFlow = new TextFlow()
            textFlow.prefWidthProperty().bind(contentPane.widthProperty())
            sentence.foreach(word =>  {
                textFlow.getChildren.add(new Text(" "))
                val wordText = new Text(word)
                wordText.hnd(MouseEvent.MOUSE_ENTERED_TARGET)(e => wordText.setFill(Color.BLUE))
                wordText.hnd(MouseEvent.MOUSE_EXITED_TARGET)(e => wordText.setFill(Color.BLACK))
                textFlow.getChildren.add(wordText)
            })
            textFlow.getChildren.add(new Text("."))
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
        }

    }
}
