package org.igye.learnpl2.controllers

import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextArea}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.VBox
import javafx.stage.Stage

import org.igye.jfxutils.action._
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.{Initable, JfxUtils}
import org.igye.learnpl2.models.LoadTextModel

@FxmlFile("fxml/LoadTextWindow.fxml")
class LoadTextController extends Initable {
    private var model: LoadTextModel = _
    var stage: Stage = _
    @FXML
    protected var loadTextWindow: VBox = _
    def getLoadTextWindow = loadTextWindow
    @FXML
    protected var textArea: TextArea = _
    @FXML
    protected var cancelBtn: Button = _
    @FXML
    protected var loadBtn: Button = _

    var onLoadButtonPressed: EventHandler[ActionEvent] = _

    private val cancelAction = new Action {
        override val description = "Cancel"
        setShortcut(Shortcut(ESCAPE))
        override protected[this] def onAction(): Unit = {
            close()
        }
    }

    private val loadAction = new Action {
        override val description = "Load"
        setShortcut(Shortcut(CONTROL, ENTER))
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

    def bindModel(model: LoadTextModel): Unit = {
        this.model = model
        textArea.textProperty().bindBidirectional(model.getTextProperty)
    }

    def getModel = model
}
