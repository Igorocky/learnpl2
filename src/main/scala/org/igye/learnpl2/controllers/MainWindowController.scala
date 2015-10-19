package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
import java.util.Random
import javafx.beans.property.ReadOnlyProperty
import javafx.event.{ActionEvent, Event}
import javafx.fxml.FXML
import javafx.scene.control.{Button, Tab, TabPane, TextField}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, Text, TextFlow}
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.commonutils.{Enum, FutureLoggable}
import org.igye.jfxutils._
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.learnpl2.TextFunctions

import scala.concurrent.ExecutionContext.Implicits.global

@FxmlFile("fxml/MainWindow.fxml")
class MainWindowController extends Initable {
    implicit val log: Logger = LogManager.getLogger()
    implicit val spellCheckerLog = Some(LogManager.getLogger("spellChecker"))

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
    @FXML
    protected var loadTextBtn: Button = _
    @FXML
    protected var backBtn: Button = _
    @FXML
    protected var nextBtn: Button = _
    @FXML
    protected var selectPrevWordBtn: Button = _
    @FXML
    protected var selectNextWordBtn: Button = _
    @FXML
    protected var translateBtn: Button = _

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
    private var words: List[Text] = _
    private var inputs: List[TextField] = _
    private var hiddenWords: List[String] = _
    private val random = new Random()
    private var selectedWordIdx = -1

    private val loadTextAction = new Action {
        override val description: String = "Load text"
        setShortcut(Shortcut(KeyCode.ALT, KeyCode.L))
        override protected[this] def onAction(): Unit = {
            loadTextStage.show()
        }
    }

    private val selectNextWordAction = new Action {
        override val description: String = "Select next word"
        setShortcut(Shortcut(KeyCode.RIGHT))
        override protected[this] def onAction(): Unit = {
            selectNextWord(1)
        }
    }

    private val selectPrevWordAction = new Action {
        override val description: String = "Select prev word"
        setShortcut(Shortcut(KeyCode.LEFT))
        override protected[this] def onAction(): Unit = {
            selectNextWord(-1)
        }
    }

    private val translateAction = new Action {
        override val description: String = "Translate"
        setShortcut(Shortcut(KeyCode.F4))
        override protected[this] def onAction(): Unit = {
            if (selectedWordIdx >= 0) {
                translateWord(words(selectedWordIdx).getText)
            }
        }
    }

    private val nextActionShortcut = Shortcut(KeyCode.ENTER)
    private val nextAction = new Action {
        override val description: String = "Next"
        setShortcut(nextActionShortcut)
        override protected[this] def onAction(): Unit = {
            if (text != null) {
                if (currState == ONLY_TEXT) {
                    showTextWithInputs()
                    currState = TEXT_WITH_INPUTS
                    currValidationStage = FILL_INPUTS
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
    }

    private val backAction = new Action {
        override val description: String = "Back"
        setShortcut(Shortcut(KeyCode.CONTROL, KeyCode.ALT, KeyCode.LEFT))
        override protected[this] def onAction(): Unit = {
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
    }

    private val actions = List(
        loadTextAction
        ,selectNextWordAction
        ,selectPrevWordAction
        ,translateAction
        ,nextAction
        ,backAction
    )

    override def init(): Unit = {
        require(mainWindow != null)
        require(contentPane != null)
        require(tabPane != null)
        require(mainTab != null)
        require(textFlow != null)
        require(loadTextBtn != null)
        require(backBtn != null)
        require(nextBtn != null)
        require(selectPrevWordBtn != null)
        require(selectNextWordBtn != null)
        require(translateBtn != null)

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

        Action.bind(loadTextAction, loadTextBtn)
        Action.bind(selectNextWordAction, selectNextWordBtn)
        Action.bind(selectPrevWordAction, selectPrevWordBtn)
        Action.bind(translateAction, translateBtn)
        Action.bind(nextAction, nextBtn)
        Action.bind(backAction, backBtn)
        JfxUtils.bindShortcutActionTrigger(mainTab, actions)
    }

    @FXML
    protected def loadTextButtonPressed(event: ActionEvent): Unit = {
        loadTextAction.trigger()
    }

    @FXML
    protected def nextButtonPressed(event: ActionEvent): Unit = {
        nextAction.trigger()
    }

    @FXML
    protected def backButtonPressed(event: ActionEvent): Unit = {
        backAction.trigger()
    }

    private def parseText(text: String): List[List[String]] = {
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts(_))
    }

    val wordClickHandler = JfxUtils.eventHandler(MouseEvent.MOUSE_CLICKED){e =>
//        val browser = FxmlSupport.load[BrowserTabController]("fxml/BrowserTab.fxml")
//        val tab = browser.getTab(e.getSource.asInstanceOf[Text].getText)
//        tabPane.getTabs.add(tab)
//        tabPane.getSelectionModel.select(tab)

        selectedWordIdx = words.indexOf(e.getTarget)
        translateAction.trigger()
    }

    private def translateWord(word: String): Unit = {
        Desktop.getDesktop().browse(new URL(s"https://translate.google.ru/#pl/ru/$word").toURI());
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
            words = Nil
            selectedWordIdx = -1
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            sentence.foreach(word =>  {
                textFlow.getChildren.add(saveWordToList(createTextElem(word)))
            })
            contentPane.getChildren.clear()
            contentPane.getChildren.add(textFlow)
            words = words.reverse
        }
    }

    def saveWordToList(word: Text) = {
        words ::= word
        word
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
                TextFunctions.checkUserInput(hiddenWords(nextIdx), inputs(nextIdx).getText, spellCheckerLog)
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
            if (!TextFunctions.checkUserInput(hiddenWords(currInputNumber), inputs(currInputNumber).getText, spellCheckerLog)) {
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
        if (TextFunctions.checkUserInput(hiddenWords(idx), inputs(idx).getText, spellCheckerLog)) {
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

    private def createTextField(): TextField = {
        val textField = new TextField()
        textField.setPrefWidth(200)
        textField.hnd(textFieldEnterPressedHnd)
        textField.focusedProperty().asInstanceOf[ReadOnlyProperty[Boolean]].addListener(CngListener[Boolean]{v =>
            if (v.newValue) {
                nextAction.removeShortcut()
            } else {
                nextAction.setShortcut(nextActionShortcut)
            }
        })
        textField
    }

    private def showTextWithInputs(): Unit = {
        RunInJfxThread {
            val sentence = text(currSentenceIdx)
            textFlow.getChildren.clear()
            inputs = Nil
            hiddenWords = Nil
            sentence.foreach(word =>  {
                if (TextFunctions.isHiddable(word) && random.nextInt(100) < 10) {
                    val textField = createTextField()
                    inputs ::= textField
                    hiddenWords ::= word
                    textFlow.getChildren.add(textField)
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

    def selectNextWordBtnPressed(event: ActionEvent): Unit = {
        selectNextWordAction.trigger()
    }

    def selectPrevWordBtnPressed(event: ActionEvent): Unit = {
        selectPrevWordAction.trigger()
    }

    def selectNextWord(step: Int): Unit = {
        def incSelectedWordIdx() = {
            selectedWordIdx += step
            if (selectedWordIdx > text(currSentenceIdx).length - 1) {
                selectedWordIdx = -1
            } else if (selectedWordIdx < -1) {
                selectedWordIdx = text(currSentenceIdx).length - 1
            }
        }
        def isCurrSelectedWordIdxAppropriate() = {
            selectedWordIdx == -1 || TextFunctions.isHiddable(text(currSentenceIdx)(selectedWordIdx))
        }
        selectWord(selectedWordIdx, FontWeight.NORMAL)
        incSelectedWordIdx()
        while(!isCurrSelectedWordIdxAppropriate()) {
            incSelectedWordIdx()
        }
        selectWord(selectedWordIdx, FontWeight.BOLD)
    }

    private def selectWord(wordIdx: Int, weight: FontWeight): Unit = {
        if (wordIdx >= 0) {
            val word = words(wordIdx)
            val font = word.getFont
            word.setFont(Font.font(font.getFamily, weight, font.getSize))
        }
    }

    def translateSelectedWordBtnPressed(event: ActionEvent): Unit = {
        translateAction.trigger()
    }
}
