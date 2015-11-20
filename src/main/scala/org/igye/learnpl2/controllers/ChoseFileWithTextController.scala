package org.igye.learnpl2.controllers

import java.io.File
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.StackPane
import javafx.stage.{FileChooser, Modality}

import org.apache.commons.lang3.StringUtils
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
    @FXML
    protected var openDialogBtn: Button = _

    private val fileChooser = new FileChooser()

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
        require(openDialogBtn != null)

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

    def openChooseFileDialog(event: ActionEvent): Unit = {
        fileChooser.setInitialDirectory(getInitialDirectory)
        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            filePathTextField.setText(file.getAbsolutePath)
        }
    }

    private def getInitialDirectory: File = {
        val file = new File(StringUtils.defaultIfEmpty(filePathTextField.getText, ""))
        if (file.exists()) {
            if (file.isDirectory) {
                file
            } else {
                file.getParentFile
            }
        } else {
            null
        }
    }
}
