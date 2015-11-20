package org.igye.learnpl2.controllers

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.StackPane
import javafx.stage.Modality

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.action.ActionType.HANDLER
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.{JfxUtils, Window, propertyToPropertyOperators}
import org.igye.learnpl2.models.ChoseFileWithTextModel
import org.igye.learnpl2.models.impl.ChoseFileWithTextModelImpl

import scala.concurrent.ExecutionContext.Implicits.global

@FxmlFile("fxml/ChoseFileWithText.fxml")
class ChoseFileWithTextController extends Window with Initable {
    implicit val log: Logger = LogManager.getLogger()

    val model: ChoseFileWithTextModel = new ChoseFileWithTextModelImpl()

    @FXML
    protected var rootNode: StackPane = _
    @FXML
    protected var filePathTextField: TextField = _
    @FXML
    protected var cancelBtn: Button = _

    private val cancelAction = new Action {
        override val description = "Cancel"
        setShortcut(Shortcut(ESCAPE))
        actionType = HANDLER
        override protected def onAction(): Unit = {
            close()
        }
    }

    private val actions = List(
        cancelAction
    )

    override def init(): Unit = {
        require(rootNode != null)
        require(filePathTextField != null)

        initWindow(rootNode)
        stage.initModality(Modality.APPLICATION_MODAL)

        Action.bind(cancelAction, cancelBtn)
        JfxUtils.bindShortcutActionTrigger(rootNode, actions)

        JfxUtils.bindFileChooser(filePathTextField, 300, 300)

        bindModel()
    }

    private def bindModel(): Unit = {
        filePathTextField.textProperty() <==> model.filePath
    }
}
