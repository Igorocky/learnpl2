package org.igye.learnpl2

import java.util.Random
import javafx.event.{ActionEvent, Event}
import javafx.fxml.FXML
import javafx.scene.control.{Tab, TabPane, TextField}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}

import org.igye.commonutils.{Enum, FutureLoggable}
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

    case class State(name: String)
    private object State extends Enum[State] {
        val NOT_LOADED = addElem(State("NOT_LOADED"))
        val ONLY_TEXT = addElem(State("ONLY_TEXT"))
        val TEXT_WITH_INPUTS = addElem(State("TEXT_WITH_INPUTS"))
    }
    import State._

    var currState = NOT_LOADED
    @volatile
    private var text: List[List[String]] = _
    private var currSentenceIdx: Int = _
    private var inputs: List[TextField] = _
    private var hiddenWords: List[String] = _
    private val random = new Random()

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
                currState = ONLY_TEXT
            }
        }
    }

    @FXML
    private def loadTextButtonPressed(event: ActionEvent): Unit = {
        loadTextStage.show()
    }

    @FXML
    protected def nextButtonPressed(event: ActionEvent): Unit = {
        if (text != null) {
            if (currState == ONLY_TEXT) {
                showTextWithInputs()
                currState = TEXT_WITH_INPUTS
            } else if (currState == TEXT_WITH_INPUTS) {
                if (currSentenceIdx < text.size - 1) {
                    currSentenceIdx += 1
                    showOnlyText()
                    currState = ONLY_TEXT
                } else {
                    loadTextButtonPressed(null)
                }
            }
        }
    }

    @FXML
    private def backButtonPressed(event: ActionEvent): Unit = {
        if (text != null) {
            if (currState == TEXT_WITH_INPUTS) {
                showOnlyText()
                currState = ONLY_TEXT
            } else if (currState == ONLY_TEXT) {
                if (currSentenceIdx > 0) {
                    currSentenceIdx -= 1
                    showTextWithInputs()
                    currState = TEXT_WITH_INPUTS
                } else {
                    loadTextButtonPressed(null)
                }
            }
        }
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

    val wordMouseEntered = JfxUtils.eventHandler(MouseEvent.MOUSE_ENTERED_TARGET){e =>
        e.getSource.asInstanceOf[Text].setFill(Color.BLUE)
    }

    val wordMouseExited = JfxUtils.eventHandler(MouseEvent.MOUSE_EXITED_TARGET){e =>
        e.getSource.asInstanceOf[Text].setFill(Color.BLACK)
    }

    private def showOnlyText(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            sentence.foreach(word =>  {
                textFlow.getChildren.add(new Text(" "))
                val wordText = new Text(word)
                wordText.hnd(wordMouseEntered, wordMouseExited, wordClickHandler)
                textFlow.getChildren.add(wordText)
            })
            textFlow.getChildren.add(new Text("."))
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
        }
    }

    val textFieldEnterPressedHnd = JfxUtils.eventHandler(KeyEvent.KEY_PRESSED){e =>
        if (e.getCode == KeyCode.ENTER) {
            val currWordNumber = inputs.indexOf(e.getSource)
            if (currWordNumber < inputs.size - 1) {
                RunInJfxThreadForcibly {
                    inputs(currWordNumber + 1).requestFocus()
                }
            } else {
                var hasWrongWords = false
                var firstWrongWord = -1
                for (i <- 0 until inputs.size) {
                    if (hiddenWords(i) != inputs(i).getText) {
                        inputs(i).setBorder(JfxUtils.createBorder(Color.RED))
                        if (!hasWrongWords) {
                            hasWrongWords = true
                            firstWrongWord = i
                        }
                    } else {
                        inputs(i).setBorder(JfxUtils.createBorder(Color.GREEN))
                    }
                }
                if (!hasWrongWords) {
                    nextButtonPressed(null)
                } else {
                    RunInJfxThreadForcibly {
                        val input = inputs(firstWrongWord)
                        input.requestFocus()
                        input.positionCaret(input.getText.length)
                    }
                }
            }
        }
    }

    private def showTextWithInputs(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            inputs = Nil
            hiddenWords = Nil
            sentence.foreach(word =>  {
                textFlow.getChildren.add(new Text(" "))
                if (random.nextInt(100) < 10) {
                    val textField = new TextField()
                    inputs ::= textField
                    hiddenWords ::= word
                    textField.setPrefWidth(200)
                    textFlow.getChildren.add(textField)
                    textField.hnd(textFieldEnterPressedHnd)
                } else {
                    val wordText = new Text(word)
                    wordText.hnd(wordMouseEntered, wordMouseExited, wordClickHandler)
                    textFlow.getChildren.add(wordText)
                }
            })
            inputs = inputs.reverse
            hiddenWords = hiddenWords.reverse
            textFlow.getChildren.add(new Text("."))
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
            if (inputs.isEmpty) {
                nextButtonPressed(null)
            } else {
                RunInJfxThreadForcibly {
                    inputs(0).requestFocus()
                }
            }
        }
    }

    def onMainTabCloseRequest(event: Event) = {
        event.consume()
    }
}
