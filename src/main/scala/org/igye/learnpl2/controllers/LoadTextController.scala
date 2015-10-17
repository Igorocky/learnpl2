package org.igye.learnpl2.controllers

import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextArea}
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.stage.Stage

import org.igye.jfxutils.{JfxUtils, Initable}
import org.igye.jfxutils.action.{Shortcut, Action}
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
    @FXML
    protected var cancelBtn: Button = _
    @FXML
    protected var loadBtn: Button = _

    var onLoadButtonPressed: EventHandler[ActionEvent] = _

    private val cancelAction = new Action {
        override val description: String = "Cancel"
        setShortcut(Shortcut(KeyCode.ESCAPE))
        override protected[this] def onAction(): Unit = {
            close()
        }
    }

    private val loadAction = new Action {
        override val description: String = "Cancel"
        setShortcut(Shortcut(KeyCode.CONTROL, KeyCode.ENTER))
        override protected[this] def onAction(): Unit = {
            onLoadButtonPressed.handle(null)
        }
    }

    private val actions = List(
        cancelAction
        ,loadAction
    )

    override def init(): Unit = {
        require(loadTextWindow != null)
        require(textArea != null)
        require(cancelBtn != null)
        require(loadBtn != null)

        Action.bind(cancelAction, cancelBtn)
        Action.bind(loadAction, loadBtn)
        JfxUtils.bindShortcutActionTrigger(loadTextWindow, actions)
    }

    @FXML
    private def loadButtonPressed(event: ActionEvent): Unit = {
        loadAction.trigger()
    }

    @FXML
    private def cancelButtonPressed(event: ActionEvent): Unit = {
        cancelAction.trigger()
    }

    def close(): Unit = {
        stage.close()
    }
}
