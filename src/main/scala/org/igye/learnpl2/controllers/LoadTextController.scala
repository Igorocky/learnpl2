package org.igye.learnpl2.controllers

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextArea}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.VBox
import javafx.stage.Modality

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.action._
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.concurrency.RunInJfxThreadForcibly
import org.igye.jfxutils.fxml.{FxmlSupport, Initable}
import org.igye.jfxutils.{JfxUtils, Window, propertyToPropertyOperators}
import org.igye.learnpl2.models.LoadTextModel
import org.igye.learnpl2.models.impl.LoadTextModelImpl

@FxmlFile("fxml/LoadTextWindow.fxml")
class LoadTextController extends Window with Initable {
    implicit val log: Logger = LogManager.getLogger()

    val model: LoadTextModel = new LoadTextModelImpl()
    @FXML
    protected var loadTextWindow: VBox = _
    @FXML
    protected var textArea: TextArea = _
    @FXML
    protected var fromFileBtn: Button = _
    @FXML
    protected var cancelBtn: Button = _
    @FXML
    protected var loadBtn: Button = _

    var onLoadButtonPressed: () => Unit = _

    private val chooseFileWithTextController: ChoseFileWithTextController = FxmlSupport.load[ChoseFileWithTextController]

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
            onLoadButtonPressed()
        }
    }

    private val loadFromFileAction = new Action {
        override val description = "Load text from file"
        setShortcut(Shortcut(ALT, O))
        override protected def onAction(): Unit = {
            chooseFileWithTextController.open()
        }
    }

    private val actions = List(
        cancelAction
        ,loadAction
        ,loadFromFileAction
    )

    override def init(): Unit = {
        require(loadTextWindow != null)
        require(textArea != null)
        require(fromFileBtn != null)
        require(cancelBtn != null)
        require(loadBtn != null)

        initWindow(loadTextWindow)
        stage.initModality(Modality.APPLICATION_MODAL)

        initChooseFileController()
        bindModel()

        Action.bind(cancelAction, cancelBtn)
        Action.bind(loadAction, loadBtn)
        Action.bind(loadFromFileAction, fromFileBtn)
        JfxUtils.bindShortcutActionTrigger(loadTextWindow, actions)
    }

    def bindModel(): Unit = {
        textArea.textProperty() <==> model.text
    }

    override def open(): Unit = {
        super.open()
        RunInJfxThreadForcibly {
            textArea.requestFocus()
        }
    }

    private def initChooseFileController(): Unit = {
        chooseFileWithTextController.onOkPressed = ()=>{
            model.loadFromFile(chooseFileWithTextController.model.filePath.get())
            chooseFileWithTextController.close()
        }
    }
}
