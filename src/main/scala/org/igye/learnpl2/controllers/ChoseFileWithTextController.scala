package org.igye.learnpl2.controllers

import java.io.File
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import javafx.scene.input.KeyCode._
import javafx.scene.layout.StackPane
import javafx.stage.Modality

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.action.ActionType.HANDLER
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.autocomplete._
import org.igye.jfxutils.fxml.Initable
import org.igye.jfxutils.{JfxUtils, Window, propertyToPropertyOperators}
import org.igye.learnpl2.TextFunctions
import org.igye.learnpl2.TextFunctions.{GeneralCaseInsensitiveStringFilter, PathAndFilter}
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

        val font = filePathTextField.getFont
        val pathPat = """(.+)""".r
        AutocompleteList.addAutocomplete(
            textField = filePathTextField,
            width = 300,
            minHeight = 30,
            prefHeight = 300,
            stackPane = rootNode,
            calcInitParams = (initStr, pos) => {
                val pathAndFilter = TextFunctions.extractPathAndFilter(initStr.substring(0, pos))
                TextFieldAutocompleteInitParams(
                    caretPositionToOpenListAt = pathAndFilter.path.length,
                    query = new BasicAutocompleteQuery(() => {
                        val filter = new GeneralCaseInsensitiveStringFilter(pathAndFilter.filter)
                        if (pathAndFilter.path.isEmpty) {
                            File.listRoots()
                                .filter(f => filter.matches(f.getAbsolutePath))
                                .sortWith((f1, f2) => f1.getName.compareTo(f2.getName) < 0)
                                .map(f => new AutocompleteTextItem(f.getAbsolutePath.replaceAllLiterally("\\", "/"), font)).toList
                        } else {
                            val path = new File(pathAndFilter.path)

                            if (path.exists()) {
                                path.listFiles()
                                    .filter(f => filter.matches(f.getName))
                                    .sortWith((f1, f2) =>
                                        f1.isDirectory && !f2.isDirectory
                                            || (
                                            (f1.isDirectory && f2.isDirectory || !f1.isDirectory && !f2.isDirectory)
                                                && f1.getName.compareTo(f2.getName) < 0
                                            )
                                    )
                                    .map(f => new AutocompleteTextItem(f.getName + (if (f.isDirectory) "/" else ""), font)).toList
                            } else {
                                List(new AutocompleteTextItem("Error: directory doesn't exist.", font, Some(false)))
                            }
                        }
                    }),
                    userData = pathAndFilter
                )
            },
            modifyTextFieldWithResultParams = (userData, item) => {
                val path = if (userData.asInstanceOf[PathAndFilter].path != null) userData.asInstanceOf[PathAndFilter].path else ""
                val filter = if (userData.asInstanceOf[PathAndFilter].filter != null) userData.asInstanceOf[PathAndFilter].filter else ""
                if (item.asInstanceOf[AutocompleteTextItem].userData.exists(!_.asInstanceOf[Boolean])) {
                    val newFullPath = path + filter
                        ModifyTextFieldWithResultParams(newFullPath, newFullPath.length)
                } else {
                    val newFullPath = path + item.asInstanceOf[AutocompleteTextItem].text
                    ModifyTextFieldWithResultParams(newFullPath, newFullPath.length)
                }
            }
        )

        bindModel()
    }

    private def bindModel(): Unit = {
        filePathTextField.textProperty() <==> model.filePath
    }
}
