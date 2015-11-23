package org.igye.learnpl2.controllers

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextArea}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.VBox
import javafx.stage.Modality

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.propertyToPropertyOperators
import org.igye.jfxutils.action._
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.concurrency.RunInJfxThreadForcibly
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.learnpl2.models.LoadTextModel
import org.igye.learnpl2.models.impl.LoadTextModelImpl
import org.igye.learnpl2.settings.Settings

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
        setShortcut(Shortcut(CONTROL, O))
        override protected def onAction(): Unit = {
            Dialogs.chooseFileDialog.open(
                Settings.directoryWithTexts,
                path => {
                    model.loadFromFile(path)
                }
            )
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
}
