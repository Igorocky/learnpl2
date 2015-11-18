package org.igye.learnpl2.controllers

import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.control.{Button, TextArea}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.VBox
import javafx.stage.{Modality, Stage}

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.action._
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.{JfxUtils, propertyToPropertyOperators}
import org.igye.learnpl2.models.LoadTextModel
import org.igye.learnpl2.models.impl.LoadTextModelImpl

@FxmlFile("fxml/LoadTextWindow.fxml")
class LoadTextController extends Initable {
    implicit val log: Logger = LogManager.getLogger()

    private var model: LoadTextModel = new LoadTextModelImpl()
    var stage: Stage = new Stage()
    @FXML
    protected var loadTextWindow: VBox = _
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

        stage.setScene(new Scene(loadTextWindow))
        stage.initModality(Modality.APPLICATION_MODAL)

        Action.bind(cancelAction, cancelBtn)
        Action.bind(loadAction, loadBtn)
        JfxUtils.bindShortcutActionTrigger(loadTextWindow, actions)

        bindModel()
    }

    @FXML
    private def loadButtonPressed(event: ActionEvent): Unit = {
        loadAction.trigger()
    }

    @FXML
    private def cancelButtonPressed(event: ActionEvent): Unit = {
        cancelAction.trigger()
    }

    def open(): Unit = {
        stage.show()
    }

    def close(): Unit = {
        stage.close()
    }

    def bindModel(): Unit = {
        textArea.textProperty() <==> model.text
    }

    def getModel = model
}
