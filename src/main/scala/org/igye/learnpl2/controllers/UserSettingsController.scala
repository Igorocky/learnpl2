package org.igye.learnpl2.controllers

import java.io.File
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.input.KeyCode._
import javafx.scene.layout.StackPane
import javafx.stage.{DirectoryChooser, Modality}

import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.observableValueToObservableValueOperators
import org.igye.jfxutils.action.{Action, ActionType, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.dialog.FileChooserType
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.properties.ChgListener
import org.igye.jfxutils.{JfxUtils, Window}
import org.igye.learnpl2.settings.Settings

import scala.concurrent.ExecutionContext.Implicits.global

@FxmlFile("fxml/UserSettings.fxml")
class UserSettingsController extends Window with Initable {
    implicit val log: Logger = LogManager.getLogger()

    @FXML
    protected var rootNode: StackPane = _
    @FXML
    protected var settsLoadedFromLabel: Label = _
    @FXML
    protected var settsLoadedFromPathLabel: Label = _
    @FXML
    protected var loadFromFileBtn: Button = _
    @FXML
    protected var dirWithTextsTextField: TextField = _
    @FXML
    protected var chooseDirBtn: Button = _
    @FXML
    protected var urlTextField: TextField = _
    @FXML
    protected var probabilityTextField: TextField = _
    @FXML
    protected var randomOrderChbx: CheckBox = _
    @FXML
    protected var skipReadingStageChbx: CheckBox = _
    @FXML
    protected var closeBtn: Button = _
    @FXML
    protected var saveAsBtn: Button = _
    @FXML
    protected var saveBtn: Button = _

    private val dirChooser = new DirectoryChooser

    private val closeAction = new Action {
        override val description = "Cancel"
        actionType = ActionType.HANDLER
        setShortcut(Shortcut(ESCAPE))
        override protected def onAction(): Unit = {
            close()
        }
    }

    private val loadFromFileAction = new Action {
        override val description = "Load settings from a file"
        setShortcut(Shortcut(CONTROL, O))
        override protected def onAction(): Unit = {
            val file = new File(Settings.userSettingsFilePath)
            var dir = ""
            if (file.exists()) {
                dir = file.getParentFile.getAbsolutePath + "/"
            }
            Dialogs.chooseFileDialog.open(
                dir,
                path => {
                    val newFile = new File(path)
                    if (newFile.exists()) {
                        Settings.userSettingsFilePath = path
                        Settings.loadUserSettings()
                        Settings.saveAppSettings()
                        updateUI()
                    } else {
                        new Alert(AlertType.ERROR, s"File '$path' doesn't exist.", ButtonType.OK).showAndWait()
                    }
                }
            )
        }
    }

    private val saveAction = new Action {
        override val description = "Save settings"
        setShortcut(Shortcut(CONTROL, S))
        override protected def onAction(): Unit = {
            if (StringUtils.isBlank(Settings.userSettingsFilePath)) {
                new Alert(AlertType.INFORMATION, s"Use 'Save as' button.", ButtonType.OK).showAndWait()
            } else {
                readValuesFromUI()
                Settings.saveUserSettings()
                updateUI()
                new Alert(AlertType.INFORMATION, s"Saved.", ButtonType.OK).showAndWait()
            }
        }
    }

    private val saveEnterAction = new Action {
        override val description = "Save settings"
        setShortcut(Shortcut(ENTER))
        actionType = ActionType.HANDLER
        override protected def onAction(): Unit = {
            saveAction.trigger()
        }
    }

    private val saveAsAction = new Action {
        override val description = "Save settings as"
        setShortcut(Shortcut(CONTROL, ALT, S))
        override protected def onAction(): Unit = {
            Dialogs.chooseFileDialog.open(
                Settings.userSettingsFilePath,
                path => {
                    val newFile = new File(path)
                    val parentOfNewFile = newFile.getParentFile
                    if (!parentOfNewFile.exists()) {
                        if (!parentOfNewFile.mkdirs()) {
                            new Alert(AlertType.ERROR, s"Can't create file '$path'.", ButtonType.OK).showAndWait()
                        }
                    }
                    if (parentOfNewFile.exists()) {
                        Settings.userSettingsFilePath = path
                        Settings.saveAppSettings()
                        readValuesFromUI()
                        Settings.saveUserSettings()
                        updateUI()
                        new Alert(AlertType.INFORMATION, s"Saved.", ButtonType.OK).showAndWait()
                    }
                }
            )
        }
    }

    private val actions = List(
        closeAction
        ,loadFromFileAction
        ,saveAction
        ,saveEnterAction
        ,saveAsAction
    )

    override def init(): Unit = {
        require(rootNode != null)
        require(settsLoadedFromLabel != null)
        require(settsLoadedFromPathLabel != null)
        require(loadFromFileBtn != null)
        require(dirWithTextsTextField != null)
        require(chooseDirBtn != null)
        require(urlTextField != null)
        require(probabilityTextField != null)
        require(randomOrderChbx != null)
        require(skipReadingStageChbx != null)
        require(closeBtn != null)
        require(saveAsBtn != null)
        require(saveBtn != null)

        initWindow(rootNode)
        stage.initModality(Modality.APPLICATION_MODAL)

        Action.bind(closeAction, closeBtn)
        Action.bind(loadFromFileAction, loadFromFileBtn)
        Action.bind(saveAction, saveBtn)
        Action.bind(saveAsAction, saveAsBtn)
        JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)

        JfxUtils.bindFileChooser(dirWithTextsTextField, 300, 300, FileChooserType.DIRS_ONLY)
        JfxUtils.bindVarNameAutocomplete(urlTextField, 100, 100, List("word"))

        val integerPat = """^\d+$""".r
        probabilityTextField.textProperty() ==> ChgListener{chg=>
            if (integerPat.findFirstIn(chg.newValue).isEmpty || chg.newValue.toInt < 0 || chg.newValue.toInt > 100) {
                new Alert(AlertType.ERROR, s"Probability should be integer number from 0 to 100", ButtonType.OK).showAndWait()
                probabilityTextField.setText(chg.oldValue)
            }
        }
    }

    private def updateUI(): Unit = {
        if (StringUtils.isBlank(Settings.userSettingsFilePath)) {
            settsLoadedFromLabel.setVisible(false)
            settsLoadedFromPathLabel.setVisible(false)
        } else {
            settsLoadedFromLabel.setVisible(true)
            settsLoadedFromPathLabel.setVisible(true)
            settsLoadedFromPathLabel.setText(Settings.userSettingsFilePath)
        }
        dirWithTextsTextField.setText(Settings.directoryWithTexts)
        urlTextField.setText(Settings.urlForTranslation)
        probabilityTextField.setText(Settings.probabilityPercent.toString)
        randomOrderChbx.setSelected(Settings.randomOrderOfSentences)
        skipReadingStageChbx.setSelected(Settings.skipReadingStage)
    }

    private def readValuesFromUI(): Unit = {
        Settings.directoryWithTexts = dirWithTextsTextField.getText
        Settings.urlForTranslation = urlTextField.getText
        Settings.probabilityPercent = probabilityTextField.getText.toInt
        Settings.randomOrderOfSentences = randomOrderChbx.isSelected
        Settings.skipReadingStage = skipReadingStageChbx.isSelected
    }

    override def open(): Unit = {
        updateUI()
        super.open()
    }

    def openChooseDirDialog(event: ActionEvent): Unit = {
        dirChooser.setInitialDirectory(getInitialDirectory)
        val file = dirChooser.showDialog(stage)
        if (file != null) {
            dirWithTextsTextField.setText(file.getAbsolutePath)
        }
    }

    private def getInitialDirectory: File = {
        val file = new File(StringUtils.defaultIfEmpty(dirWithTextsTextField.getText, ""))
        if (file.exists()) {
            file
        } else {
            null
        }
    }
}
