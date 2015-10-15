package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
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
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.learnpl2.TextFunctions
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global

@FxmlFile("fxml/MainWindow.fxml")
class MainWindowController extends Initable {
    implicit val log: Logger = LoggerFactory.getLogger(this.getClass)

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

    case class ValidationStage(name: String)
    private object ValidationStage extends Enum[ValidationStage] {
        val FILL_INPUTS = addElem(ValidationStage("FILL_INPUTS"))
        val CORRECTIONS_STAGE = addElem(ValidationStage("CORRECTIONS_STAGE"))
    }
    import ValidationStage._

    var currState = NOT_LOADED
    var currValidationStage = FILL_INPUTS
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

        val loadTextWnd = FxmlSupport.load[LoadTextController]
        loadTextWnd.stage = loadTextStage
        loadTextStage.setScene(new Scene(loadTextWnd.getLoadTextWindow))
        loadTextStage.initModality(Modality.APPLICATION_MODAL)

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
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts(_))
    }

    val wordClickHandler = JfxUtils.eventHandler(MouseEvent.MOUSE_CLICKED){e =>
//        val browser = FxmlSupport.load[BrowserTabController]("fxml/BrowserTab.fxml")
//        val tab = browser.getTab(e.getSource.asInstanceOf[Text].getText)
//        tabPane.getTabs.add(tab)
//        tabPane.getSelectionModel.select(tab)

        Desktop.getDesktop().browse(new URL(s"https://translate.google.ru/#pl/ru/${e.getSource.asInstanceOf[Text].getText}").toURI());
    }

    val wordMouseEntered = JfxUtils.eventHandler(MouseEvent.MOUSE_ENTERED_TARGET){e =>
        e.getSource.asInstanceOf[Text].setFill(Color.BLUE)
    }

    val wordMouseExited = JfxUtils.eventHandler(MouseEvent.MOUSE_EXITED_TARGET){e =>
        val text = e.getSource.asInstanceOf[Text]
        text.setFill(getWordColor(text.getText))
    }

    private def showOnlyText(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            sentence.foreach(word =>  {
                textFlow.getChildren.add(createTextElem(word))
            })
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
        }
    }

    def createTextElem(word: String): Text = {
        val wordText = new Text(word)
        wordText.setFill(getWordColor(word))
        wordText.hnd(getWordHandlers(word): _*)
        wordText
    }

    def getWordColor(word: String): Color = {
        if (TextFunctions.isHiddable(word)) {
            Color.GREEN
        } else {
            Color.RED
        }
    }

    def getWordHandlers(word: String): List[EventHandlerInfo[MouseEvent]] = {
        if (TextFunctions.isHiddable(word)) {
            List(wordMouseEntered, wordMouseExited, wordClickHandler)
        } else {
            Nil
        }
    }

    def findFirstInvalidWordStartingFromIdx(idx: Int): Option[Int] = {
        var nextIdx = idx
        while (
            nextIdx < inputs.size &&
                TextFunctions.checkUserInput(hiddenWords(nextIdx), inputs(nextIdx).getText, Some(log))
        ) {
            nextIdx += 1
        }
        if (nextIdx < inputs.size) {
            Some(nextIdx)
        } else {
            None
        }
    }

    def getNextInputToBeEditedInCurrValidationStage(currInput: Option[Any]): Option[TextField] = {
        val currInputNumber = if (currInput.isDefined) inputs.indexOf(currInput.get) else 0
        if (currValidationStage == FILL_INPUTS) {
            if (currInputNumber < inputs.size - 1) {
                Some(inputs(currInputNumber + 1))
            } else {
                None
            }
        } else {
            //validate current word. If it is invalid then stay on it.
            if (!TextFunctions.checkUserInput(hiddenWords(currInputNumber), inputs(currInputNumber).getText, Some(log))) {
                Some(inputs(currInputNumber))
            } else {
                //If it is valid then find next invalid word.
                var nextInvalidWord = findFirstInvalidWordStartingFromIdx(currInputNumber + 1)
                if (nextInvalidWord.isDefined) {
                    Some(inputs(nextInvalidWord.get))
                } else {
                    nextInvalidWord = findFirstInvalidWordStartingFromIdx(0)
                    if (nextInvalidWord.isDefined) {
                        Some(inputs(nextInvalidWord.get))
                    } else {
                        None
                    }
                }
            }
        }
    }

    def highlightInput(idx: Int): Unit = {
        if (TextFunctions.checkUserInput(hiddenWords(idx), inputs(idx).getText, Some(log))) {
            inputs(idx).setBorder(JfxUtils.createBorder(Color.GREEN))
        } else {
            inputs(idx).setBorder(JfxUtils.createBorder(Color.RED))
        }
    }

    def highlightInput(input: Any): Unit = {
        highlightInput(inputs.indexOf(input))
    }

    def highLightAllInputs(): Unit = {
        (0 until inputs.length).foreach(highlightInput)
    }

    val textFieldEnterPressedHnd = JfxUtils.eventHandler(KeyEvent.KEY_PRESSED){e =>
        if (e.getCode == KeyCode.ENTER) {
            val currInput = e.getSource
            val nextInputOpt = getNextInputToBeEditedInCurrValidationStage(Some(currInput))
            if (nextInputOpt.isDefined) {
                if (currValidationStage == CORRECTIONS_STAGE && currInput != nextInputOpt.get) {
                    highlightInput(currInput)
                }
                RunInJfxThreadForcibly {
                    nextInputOpt.get.requestFocus()
                }
            } else {
                if (currValidationStage == FILL_INPUTS) {
                    highLightAllInputs()
                    val firstInvalidWord = findFirstInvalidWordStartingFromIdx(0)
                    if (firstInvalidWord.isDefined) {
                        currValidationStage = CORRECTIONS_STAGE
                        RunInJfxThreadForcibly {
                            inputs(firstInvalidWord.get).requestFocus()
                        }
                    } else {
                        nextButtonPressed(null)
                    }
                } else {
                    highLightAllInputs()
                    currValidationStage = FILL_INPUTS
                    nextButtonPressed(null)
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
                if (TextFunctions.isHiddable(word) && random.nextInt(100) < 10) {
                    val textField = new TextField()
                    inputs ::= textField
                    hiddenWords ::= word
                    textField.setPrefWidth(200)
                    textFlow.getChildren.add(textField)
                    textField.hnd(textFieldEnterPressedHnd)
                } else {
                    textFlow.getChildren.add(createTextElem(word))
                }
            })
            inputs = inputs.reverse
            hiddenWords = hiddenWords.reverse
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
