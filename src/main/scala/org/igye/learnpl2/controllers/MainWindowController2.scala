package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
import javafx.beans.property.BooleanProperty
import javafx.event.{ActionEvent, Event}
import javafx.fxml.FXML
import javafx.scene.control.{Button, Tab, TabPane}
import javafx.scene.input.{KeyCode, MouseEvent}
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight, TextFlow}
import javafx.scene.{Node, Parent}

import scala.collection.JavaConversions._

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils._
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.properties.Expr
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.models.impl.MainWindowModelImpl
import org.igye.learnpl2.models.{MainWindowModel, Word}
import org.igye.learnpl2.controllers.State._

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
        e.getTarget.asInstanceOf[TextElem].word.mouseEntered.setValue(true)
    }

    val wordMouseExited = JfxUtils.eventHandler(MouseEvent.MOUSE_EXITED_TARGET){e =>
        e.getTarget.asInstanceOf[TextElem].word.mouseEntered.setValue(false)
    }

    val wordClickHandler = JfxUtils.eventHandler(MouseEvent.MOUSE_CLICKED){e =>
        //        val browser = FxmlSupport.load[BrowserTabController]("fxml/BrowserTab.fxml")
        //        val tab = browser.getTab(e.getSource.asInstanceOf[Text].getText)
        //        tabPane.getTabs.add(tab)
        //        tabPane.getSelectionModel.select(tab)

        model.selectWord(e.getTarget.asInstanceOf[TextElem].word)
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

    private val actions = List(
        loadTextAction
//        ,selectNextWordAction
//        ,selectPrevWordAction
        ,translateAction
        ,nextAction
//        ,backAction
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
//        Action.bind(backAction, backBtn)
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
        textFlow.getChildren <== (model.currSentence, createNodeFromWord, destroyNodeFromWord)
        model.currState ==> ChgListener{chg=>
            if (chg.newValue == TEXT_WITH_INPUTS) {
                RunInJfxThreadForcibly {
                    textFlow.getChildren.find(_.isInstanceOf[EditElem]).foreach(_.requestFocus())
                }
            } else if (chg.newValue == NOT_LOADED) {
                loadTextAction.trigger()
            }
        }
    }

    val hideWordListener = ChgListener[java.lang.Boolean]{chg=>
        if (chg.newValue) {
            val word = chg.observable.asInstanceOf[BooleanProperty].getBean.asInstanceOf[Word]
            val idx = model.currSentence.indexOf(word)
            textFlow.getChildren.remove(idx)
            textFlow.getChildren.add(idx, createNodeFromWord(word))
        }
    }

    private def createNodeFromWord(word: Word): Node = {
        val res = if (!word.hidden.get) createTextElem(word) else createEditElem(word)
        word.hidden ==> hideWordListener
        res
    }

    private def destroyNodeFromWord(node: Node): Unit = {
        node.asInstanceOf[WordRepr].word.hidden.removeListener(hideWordListener)
    }

    private def createTextElem(word: Word): TextElem = {
        val res = new TextElem(word)
        res.fillProperty <== Expr(word.mouseEntered) {
            if (word.mouseEntered.get) Color.BLUE else getWordColor(word)
        }
        val fontFamily = res.getFont.getFamily
        res.fontProperty() <== Expr(word.selected) {
            val weight = if (word.selected.get) FontWeight.BOLD else FontWeight.NORMAL
            Font.font(fontFamily, weight, 20.0)
        }
        res.hnd(getWordHandlers(word): _*)
        res
    }

    private def createEditElem(word: Word): EditElem = {
        val res = new EditElem(word)
        res.setPrefWidth(200)
//        res.hnd(textFieldEnterPressedHnd)
        res.focusedProperty() ==> ChgListener {chg=>
            if (chg.newValue) {
//                nextAction.removeShortcut()
            } else {
//                nextAction.setShortcut(nextActionShortcut)
            }
        }
        res
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
//        backAction.trigger()
    }

    def selectPrevWordBtnPressed(event: ActionEvent): Unit = {
//        selectPrevWordAction.trigger()
    }

    def selectNextWordBtnPressed(event: ActionEvent): Unit = {
//        selectNextWordAction.trigger()
    }

    def translateSelectedWordBtnPressed(event: ActionEvent): Unit = {
//        translateAction.trigger()
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
