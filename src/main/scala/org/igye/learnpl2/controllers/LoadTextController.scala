package org.igye.learnpl2.controllers

import java.io.File
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.layout.VBox
import javafx.stage.Modality

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.{observableValueToObservableValueOperators, propertyToPropertyOperators}
import org.igye.jfxutils.action._
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.concurrency.RunInJfxThreadForcibly
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.properties.{ChgListener, Expr}
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
    @FXML
    protected var saveAsBtn: Button = _
    @FXML
    protected var saveBtn: Button = _
    @FXML
    protected var newBtn: Button = _
    @FXML
    protected var loadedFromLbl: Label = _
    @FXML
    protected var loadedFromPathLbl: Label = _

    var onLoadButtonPressed: () => Unit = _

    private val cancelAction = new Action {
        override val description = "Cancel"
        setShortcut(Shortcut(ESCAPE))
        override protected def onAction(): Unit = {
            close()
        }
    }

    private val newAction = new Action {
        override val description = "New"
        setShortcut(Shortcut(CONTROL, N))
        override protected def onAction(): Unit = {
            model.reset()
        }
    }

    private val loadAction = new Action {
        override val description = "Load"
        setShortcut(Shortcut(CONTROL, ENTER))
        override protected def onAction(): Unit = {
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

    private val saveAction = new Action {
        override val description = "Save"
        setShortcut(Shortcut(CONTROL, S))
        override protected def onAction(): Unit = {
            if (StringUtils.isBlank(model.loadedFrom.get())) {
                new Alert(AlertType.INFORMATION, s"Use 'Save as' button.", ButtonType.OK).showAndWait()
            } else {
                model.save()
                new Alert(AlertType.INFORMATION, s"Saved.", ButtonType.OK).showAndWait()
            }
        }
    }

    private val saveAsAction = new Action {
        override val description = "Save as"
        setShortcut(Shortcut(CONTROL, ALT, S))
        override protected def onAction(): Unit = {
            Dialogs.chooseFileDialog.open(
                model.loadedFrom.get(),
                path => {
                    val newFile = new File(path)
                    val parentOfNewFile = newFile.getParentFile
                    if (!parentOfNewFile.exists()) {
                        if (!parentOfNewFile.mkdirs()) {
                            new Alert(AlertType.ERROR, s"Can't create file '$path'.", ButtonType.OK).showAndWait()
                        }
                    }
                    if (parentOfNewFile.exists()) {
                        model.saveAs(path)
                        new Alert(AlertType.INFORMATION, s"Saved.", ButtonType.OK).showAndWait()
                    }
                }
            )
        }
    }

    private val actions = List(
        cancelAction
        ,loadAction
        ,loadFromFileAction
        ,saveAsAction
        ,saveAction
        ,newAction
    )

    override def init(): Unit = {
        require(loadTextWindow != null)
        require(textArea != null)
        require(fromFileBtn != null)
        require(cancelBtn != null)
        require(loadBtn != null)
        require(saveAsBtn != null)
        require(saveBtn != null)
        require(newBtn != null)
        require(loadedFromLbl != null)
        require(loadedFromPathLbl != null)

        initWindow(loadTextWindow)
        stage.initModality(Modality.APPLICATION_MODAL)

        bindModel()

        Action.bind(cancelAction, cancelBtn)
        Action.bind(loadAction, loadBtn)
        Action.bind(loadFromFileAction, fromFileBtn)
        Action.bind(saveAsAction, saveAsBtn)
        Action.bind(saveAction, saveBtn)
        Action.bind(newAction, newBtn)
        JfxUtils.bindActionsToSceneProp(loadTextWindow.sceneProperty(), actions)
    }

    def bindModel(): Unit = {
        model.text <==> textArea.textProperty()
        model.caretPosition <== textArea.caretPositionProperty()
        model.caretPosition ==> ChgListener{chg =>
            textArea.positionCaret(chg.newValue.asInstanceOf[Int])
        }
        loadedFromLbl.visibleProperty() <== Expr(model.loadedFrom)(!StringUtils.isBlank(model.loadedFrom.get()))
        loadedFromPathLbl.visibleProperty() <== loadedFromLbl.visibleProperty()
        loadedFromPathLbl.textProperty() <== model.loadedFrom
    }

    def open(caretPosition: Int): Unit = {
        super.open()
        RunInJfxThreadForcibly {
            textArea.requestFocus()
            textArea.positionCaret(caretPosition)
        }
    }
}
