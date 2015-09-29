package org.igye.learnpl2

import javafx.event.{Event, ActionEvent}
import javafx.fxml.FXML
import javafx.scene.control.{Tab, TabPane}
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

class MainWindowController extends Initable {
    implicit val log = LoggerFactory.getLogger(this.getClass)

    var primaryStage: Stage = _

    @FXML
    protected var mainWindow: Parent = _
    def getMainWindow = mainWindow
    @FXML
    protected var contentPane: Pane = _
    @FXML
    protected var tabPane: TabPane = _
    @FXML
    protected var mainTab: Tab = _
    @FXML
    protected var textFlow: TextFlow = _

    private val loadTextStage: Stage = new Stage()

    override def init(): Unit = {
        require(mainWindow != null)
        require(contentPane != null)
        require(tabPane != null)
        require(mainTab != null)
        require(textFlow != null)

        val loadTextWnd = FxmlSupport.load[LoadTextController]("fxml/LoadTextWindow.fxml")
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
    }


    @volatile
    private var text: List[List[String]] = _
    private var currSentenceIdx: Int = _

    @FXML
    private def loadTextButtonPressed(event: ActionEvent): Unit = {
        loadTextStage.show()
    }

    private def parseText(text: String): List[List[String]] = {
        text.split("\\.\\s").map(_.split("\\s").toList).toList
    }

    val wordClickHandler = JfxUtils.eventHandler(MouseEvent.MOUSE_CLICKED){e =>
        val browser = FxmlSupport.load[BrowserTabController]("fxml/BrowserTab.fxml")
        val tab = browser.getTab(e.getSource.asInstanceOf[Text].getText)
        tabPane.getTabs.add(tab)
        tabPane.getSelectionModel.select(tab)
    }

    private def showOnlyText(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            textFlow.prefWidthProperty().bind(contentPane.widthProperty())
            sentence.foreach(word =>  {
                textFlow.getChildren.add(new Text(" "))
                val wordText = new Text(word)
                wordText.hnd(MouseEvent.MOUSE_ENTERED_TARGET)(e => wordText.setFill(Color.BLUE))
                wordText.hnd(MouseEvent.MOUSE_EXITED_TARGET)(e => wordText.setFill(Color.BLACK))
                wordText.hnd(wordClickHandler)
                textFlow.getChildren.add(wordText)
            })
            textFlow.getChildren.add(new Text("."))
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
        }
    }

    def onMainTabCloseRequest(event: Event) = {
        event.consume()
    }
}
