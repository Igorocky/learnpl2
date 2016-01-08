package org.igye.learnpl2.controllers

import java.awt.Desktop
import java.net.URL
import javafx.event.Event
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.scene.layout.FlowPane
import javafx.scene.paint.Color
import javafx.scene.text.{Font, FontWeight}

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.commonutils.Implicits.consumer
import org.igye.jfxutils.Implicits._
import org.igye.jfxutils._
import org.igye.jfxutils.action.{Action, ActionType, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.events.{EventHandlerInfo, Hnd}
import org.igye.jfxutils.fxml.{FxmlSupport, Initable}
import org.igye.jfxutils.properties.{ChgListener, Expr, UpFrontTrigger}
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.controllers.State._
import org.igye.learnpl2.models.impl.MainWindowModelImpl
import org.igye.learnpl2.models.{MainWindowModel, Word}
import org.igye.learnpl2.settings.Settings

@FxmlFile("fxml/MainWindowTexts.fxml")
class MainWindowTextsController extends Initable {
    implicit val log: Logger = LogManager.getLogger()
    implicit val spellCheckerLog = Some(LogManager.getLogger("spellChecker"))

    private val model: MainWindowModel = new MainWindowModelImpl()

    var onCloseHandler: () => Unit = _

    @FXML
    var rootPane: TabPane = _
    @FXML
    protected var mainTab: Tab = _
    @FXML
    protected var textFlow: FlowPane = _
    @FXML
    protected var loadTextBtn: Button = _
    @FXML
    protected var backBtn: Button = _
    @FXML
    protected var nextBtn: Button = _
    @FXML
    protected var repeatBtn: Button = _
    @FXML
    protected var nextSentenceBtn: Button = _
    @FXML
    protected var selectPrevWordBtn: Button = _
    @FXML
    protected var selectNextWordBtn: Button = _
    @FXML
    protected var translateBtn: Button = _
    @FXML
    protected var settingsBtn: Button = _
    @FXML
    protected var exitBtn: Button = _
    @FXML
    protected var sentenceIdxTextField: TextField = _

    private val loadTextController: LoadTextController = FxmlSupport.load[LoadTextController]
    private val userSettingsController: UserSettingsController = FxmlSupport.load[UserSettingsController]

    val wordMouseEntered = Hnd(MouseEvent.MOUSE_ENTERED_TARGET){e =>
        e.getSource.asInstanceOf[ParentHasWord].getWord.mouseEntered.setValue(true)
    }

    val wordMouseExited = Hnd(MouseEvent.MOUSE_EXITED_TARGET){e =>
        e.getSource.asInstanceOf[ParentHasWord].getWord.mouseEntered.setValue(false)
    }

    val wordClickHandler = Hnd(MouseEvent.MOUSE_CLICKED){e =>
        model.selectWord(e.getSource.asInstanceOf[ParentHasWord].getWord)
        translateAction.trigger()
    }

    private val loadTextAction = new Action {
        override val description: String = "Load text"
        setShortcut(Shortcut(CONTROL, L))
        override protected def onAction(): Unit = {
            loadTextController.open(model.selectionRange)
        }
    }

    private val loadTextF1Action = new Action {
        override val description: String = "Load text"
        setShortcut(Shortcut(F1))
        enabled <== loadTextAction.enabled
        override protected def onAction(): Unit = {
            loadTextAction.trigger()
        }
    }

    private val translateAction = new Action {
        override val description: String = "Translate"
        setShortcut(Shortcut(ALT, ENTER))
        enabled <== Expr(model.selectedWord){
            model.selectedWord.get().isDefined
        }
        override protected def onAction(): Unit = {
            model.selectedWord.get.foreach(w => translateWord(w.text))
        }
    }

    private val nextActionShortcut = Shortcut(ENTER)
    private val nextAction = new Action {
        override val description: String = "Next"
        setShortcut(nextActionShortcut)
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            model.next()
        }
    }

    private val repeatAction = new Action {
        override val description: String = "Repeat"
        setShortcut(Shortcut(CONTROL, R))
        enabled <== Expr(model.currState){
            model.currState.get == TEXT_WITH_INPUTS
        }
        override protected def onAction(): Unit = {
            backAction.trigger()
            nextAction.trigger()
        }
    }

    private val nextSentenceAction = new Action {
        override val description: String = "Next sentence"
        setShortcut(Shortcut(PAGE_DOWN))
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            model.nextSentence()
        }
    }

    private val backAction = new Action {
        override val description: String = "Back"
        setShortcut(Shortcut(PAGE_UP))
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            model.back()
            textFlow.focus()
        }
    }

    private val selectNextWordAction = new Action {
        override val description: String = "Select next word"
        setShortcut(Shortcut(ALT, RIGHT))
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            model.selectNextWord(1)
        }
    }

    private val selectPrevWordAction = new Action {
        override val description: String = "Select prev word"
        setShortcut(Shortcut(ALT, LEFT))
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            model.selectNextWord(-1)
        }
    }

    private val settingsAction = new Action {
        override val description: String = "Open settings dialog"
        setShortcut(Shortcut(ALT, S))
        override protected def onAction(): Unit = {
            userSettingsController.open()
        }
    }

    private val exitAction = new Action {
        override val description: String = "Exit"
        setShortcut(Shortcut(HOME))
        actionType = ActionType.HANDLER
        override protected def onAction(): Unit = {
            new Alert(AlertType.CONFIRMATION, s"Are you sure you want to exit?", ButtonType.NO, ButtonType.YES)
                .showAndWait().ifPresent(consumer{ t=>
                if (t == ButtonType.YES) {
                    onCloseHandler()
                }
            })
        }
    }

    private val gotoAction = new Action {
        override val description: String = "Go to sentence"
        setShortcut(Shortcut(CONTROL, G))
        enabled <== Expr(model.currState){
            model.currState.get() != NOT_LOADED
        }
        override protected def onAction(): Unit = {
            sentenceIdxTextField.focus()
            sentenceIdxTextField.selectAll()
        }
    }

    private val actions = List(
        loadTextAction
        ,loadTextF1Action
        ,selectNextWordAction
        ,selectPrevWordAction
        ,translateAction
        ,nextAction
        ,repeatAction
        ,nextSentenceAction
        ,backAction
        ,settingsAction
        ,exitAction
        ,gotoAction
    )

    override def init(): Unit = {
        require(rootPane != null)
        require(mainTab != null)
        require(textFlow != null)
        require(loadTextBtn != null)
        require(backBtn != null)
        require(nextBtn != null)
        require(repeatBtn != null)
        require(nextSentenceBtn != null)
        require(selectPrevWordBtn != null)
        require(selectNextWordBtn != null)
        require(translateBtn != null)
        require(settingsBtn != null)
        require(exitBtn != null)
        require(sentenceIdxTextField != null)

        bindModel()

        initLoadTextController()
        initSentenceIdxTextField()

        Action.bind(loadTextAction, loadTextBtn)
        Action.bind(selectNextWordAction, selectNextWordBtn)
        Action.bind(selectPrevWordAction, selectPrevWordBtn)
        Action.bind(translateAction, translateBtn)
        Action.bind(nextAction, nextBtn)
        Action.bind(repeatAction, repeatBtn)
        Action.bind(nextSentenceAction, nextSentenceBtn)
        Action.bind(backAction, backBtn)
        Action.bind(settingsAction, settingsBtn)
        Action.bind(exitAction, exitBtn)
        JfxUtils.bindActionsToSceneProp(rootPane.sceneProperty(), actions)
    }

    private def initSentenceIdxTextField(): Unit = {
        sentenceIdxTextField.focusedProperty() ==> ChgListener{chg=>
            if (chg.newValue) {
                nextAction.removeShortcut()
            } else {
                nextAction.setShortcut(nextActionShortcut)
            }
        }
        sentenceIdxTextField.setOnAction(Hnd{e =>
            val requestedSentenceIdx = sentenceIdxTextField.getText.toInt
            model.goToSentence(requestedSentenceIdx - 1)
            e.consume()
            if (model.currSentenceIdx.get() != requestedSentenceIdx - 1) {
                new Alert(AlertType.ERROR, s"${requestedSentenceIdx} is incorrect sentence number. " +
                    s"Correct values are from 1 to ${model.sentenceCount}", ButtonType.OK).showAndWait()
            }
            textFlow.focus()
        })
    }

    private def initLoadTextController(): Unit = {
        loadTextController.onLoadButtonPressed = ()=>{
            model.setText(loadTextController.model.text.get(), loadTextController.model.caretPosition.get())
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
        model.currSentenceIdx ==> ChgListener{chg=>
            if (chg.newValue.asInstanceOf[Int] >= 0) {
                sentenceIdxTextField.setText((model.currSentenceIdx.get() + 1).toString)
            } else {
                sentenceIdxTextField.setText("")
            }
        }
    }

    private def createNodeFromWord(word: Word): Node = {
        val wordRepr = new WordRepr(word, createTextElem(word), if (word.hiddable) Some(createEditElem(word)) else None)
        wordRepr.showTextField <== word.hidden
        wordRepr.setRequestFocusTrigger(new UpFrontTrigger(Expr(word.awaitingUserInput){word.awaitingUserInput.get()}))
        wordRepr
    }

    private def createTextElem(word: Word): Label with ParentHasWord = {
        val textElem = new Label(word.text) with ParentHasWord
        textElem.textFillProperty() <== Expr(word.mouseEntered) {
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
            if (e.getCode == ENTER) {
                if (textField.getText != textField.getWord.getUserInput.getOrElse(null)) {
                    textField.getWord.setUserInput(textField.getText)
                }
                model.gotoNextWordToBeEnteredOrSwitchToNextSentence()
            }
        }
        textField.focusedProperty() ==> ChgListener {chg=>
            if (chg.newValue) {
                nextAction.removeShortcut()
                model.focusWord(textField.getWord)
            } else {
                nextAction.setShortcut(nextActionShortcut)
            }
        }
        val initialBorder = textField.getBorder
        textField.borderProperty() <== Expr(word.userInputIsCorrect) {
            if (word.userInputIsCorrect.get().isEmpty) {
                initialBorder
            } else if (word.userInputIsCorrect.get().get) {
                JfxUtils.createBorder(Color.GREEN, 3)
            } else {
                JfxUtils.createBorder(Color.RED, 3)
            }
        }
        textField
    }

    def onMainTabCloseRequest(event: Event) = {
        event.consume()
    }

    def getWordColor(word: Word): Color = {
        if (word.hiddable) {
            Color.GREEN
        } else {
            Color.RED
        }
    }

    private def translateWord(word: String): Unit = {
        Desktop.getDesktop().browse(new URL(Settings.urlForTranslation.replaceAllLiterally("${word}", word)).toURI())
    }

    def getWordHandlers(word: Word): List[EventHandlerInfo[MouseEvent]] = {
        if (word.hiddable) {
            List(wordMouseEntered, wordMouseExited, wordClickHandler)
        } else {
            Nil
        }
    }
}
