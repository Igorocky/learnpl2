package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
import javafx.event.{ActionEvent, Event}
import javafx.fxml.FXML
import javafx.scene.control.{Button, Tab, TabPane, TextField}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, Text, TextFlow}
import javafx.scene.{Node, Parent}

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils._
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.properties.{Expr, UpFrontTrigger}
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.controllers.State._
import org.igye.learnpl2.models.impl.MainWindowModelImpl
import org.igye.learnpl2.models.{MainWindowModel, Word}

@FxmlFile("fxml/MainWindow2.fxml")
class MainWindowController2 extends Initable {
    implicit val log: Logger = LogManager.getLogger()
    implicit val spellCheckerLog = Some(LogManager.getLogger("spellChecker"))

    private val model: MainWindowModel = new MainWindowModelImpl()

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

    private val loadTextController: LoadTextController = FxmlSupport.load[LoadTextController]

    val wordMouseEntered = JfxUtils.eventHandler(MouseEvent.MOUSE_ENTERED_TARGET){e =>
        e.getTarget.asInstanceOf[ParentHasWord].getWord.mouseEntered.setValue(true)
    }

    val wordMouseExited = JfxUtils.eventHandler(MouseEvent.MOUSE_EXITED_TARGET){e =>
        e.getTarget.asInstanceOf[ParentHasWord].getWord.mouseEntered.setValue(false)
    }

    val wordClickHandler = JfxUtils.eventHandler(MouseEvent.MOUSE_CLICKED){e =>
        //        val browser = FxmlSupport.load[BrowserTabController]("fxml/BrowserTab.fxml")
        //        val tab = browser.getTab(e.getSource.asInstanceOf[Text].getText)
        //        tabPane.getTabs.add(tab)
        //        tabPane.getSelectionModel.select(tab)

        model.selectWord(e.getTarget.asInstanceOf[ParentHasWord].getWord)
        translateAction.trigger()
    }

    private val loadTextAction = new Action {
        override val description: String = "Load text"
        setShortcut(Shortcut(KeyCode.ALT, KeyCode.L))
        override protected[this] def onAction(): Unit = {
            loadTextController.open()
        }
    }

    private val translateAction = new Action {
        override val description: String = "Translate"
        setShortcut(Shortcut(KeyCode.F4))
        override protected[this] def onAction(): Unit = {
            model.getSelectedWord.foreach(w => translateWord(w.text))
        }
    }

    private val nextActionShortcut = Shortcut(KeyCode.ENTER)
    private val nextAction = new Action {
        override val description: String = "Next"
        setShortcut(nextActionShortcut)
        override protected[this] def onAction(): Unit = {
            model.next()
        }
    }

    private val backAction = new Action {
        override val description: String = "Back"
        setShortcut(Shortcut(KeyCode.CONTROL, KeyCode.ALT, KeyCode.LEFT))
        override protected[this] def onAction(): Unit = {
            model.back()
        }
    }

    private val actions = List(
        loadTextAction
//        ,selectNextWordAction
//        ,selectPrevWordAction
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

        bindModel()

        initLoadTextController()

        Action.bind(loadTextAction, loadTextBtn)
//        Action.bind(selectNextWordAction, selectNextWordBtn)
//        Action.bind(selectPrevWordAction, selectPrevWordBtn)
        Action.bind(translateAction, translateBtn)
        Action.bind(nextAction, nextBtn)
        Action.bind(backAction, backBtn)
        JfxUtils.bindShortcutActionTrigger(mainTab, actions)
    }

    private def initLoadTextController(): Unit = {
        loadTextController.onLoadButtonPressed = JfxActionEventHandler {e =>
            model.setText(loadTextController.getModel.text.get)
            loadTextController.close()
        }
    }

    private def parseText(text: String): List[List[String]] = {
        TextFunctions.splitTextOnSentences(text).map(TextFunctions.splitSentenceOnParts(_))
    }

    private def bindModel(): Unit = {
        textFlow.getChildren <== (model.currSentence, createNodeFromWord)
        model.currState ==> ChgListener{chg=>
            if (chg.newValue == NOT_LOADED) {
                loadTextAction.trigger()
            }
        }
    }

    private def createNodeFromWord(word: Word): Node = {
        val wordRepr = new WordRepr(word, createTextElem(word), if (word.hiddable) Some(createEditElem(word)) else None)
        wordRepr.showTextField <== word.hidden
        wordRepr.setRequestFocusTrigger(new UpFrontTrigger(Expr(word.awaitingUserInput){word.awaitingUserInput.get()}))
        wordRepr
    }

    private def createTextElem(word: Word): Text with ParentHasWord = {
        val textElem = new Text(word.text) with ParentHasWord
        textElem.fillProperty <== Expr(word.mouseEntered) {
            if (word.mouseEntered.get) Color.BLUE else getWordColor(word)
        }
        val fontFamily = textElem.getFont.getFamily
        textElem.fontProperty() <== Expr(word.selected) {
            val weight = if (word.selected.get) FontWeight.BOLD else FontWeight.NORMAL
            Font.font(fontFamily, weight, 20.0)
        }
        textElem.hnd(getWordHandlers(word): _*)
        textElem
    }

    private def createEditElem(word: Word): TextField with ParentHasWord = {
        val textField = new TextField() with ParentHasWord
        textField.setPrefWidth(200)
        textField.hnd(KeyEvent.KEY_PRESSED){e =>
            if (e.getCode == KeyCode.ENTER) {
                textField.getWord.setUserInput(textField.getText)
                model.gotoNextWordToBeEnteredOrSwitchToNextSentence()
            }
        }
        textField.focusedProperty() ==> ChgListener {chg=>
            if (chg.newValue) {
                nextAction.removeShortcut()
            } else {
                nextAction.setShortcut(nextActionShortcut)
            }
        }
        val initialBorder = textField.getBorder
        textField.borderProperty() <== Expr(word.userInputIsCorrect) {
            if (word.userInputIsCorrect.get().isEmpty) {
                initialBorder
            } else if (word.userInputIsCorrect.get().get) {
                JfxUtils.createBorder(Color.GREEN)
            } else {
                JfxUtils.createBorder(Color.RED)
            }
        }
        textField
    }

    def onMainTabCloseRequest(event: Event) = {
        event.consume()
    }

    protected def loadTextButtonPressed(event: ActionEvent): Unit = {
        loadTextAction.trigger()
    }

    protected def nextButtonPressed(event: ActionEvent): Unit = {
        nextAction.trigger()
    }

    protected def backButtonPressed(event: ActionEvent): Unit = {
        backAction.trigger()
    }

    def selectPrevWordBtnPressed(event: ActionEvent): Unit = {
//        selectPrevWordAction.trigger()
    }

    def selectNextWordBtnPressed(event: ActionEvent): Unit = {
//        selectNextWordAction.trigger()
    }

    def translateSelectedWordBtnPressed(event: ActionEvent): Unit = {
        translateAction.trigger()
    }

    def getWordColor(word: Word): Color = {
        if (word.hiddable) {
            Color.GREEN
        } else {
            Color.RED
        }
    }

    private def translateWord(word: String): Unit = {
        Desktop.getDesktop().browse(new URL(s"https://translate.google.ru/#pl/ru/$word").toURI());
    }

    def getWordHandlers(word: Word): List[EventHandlerInfo[MouseEvent]] = {
        if (word.hiddable) {
            List(wordMouseEntered, wordMouseExited, wordClickHandler)
        } else {
            Nil
        }
    }
}
