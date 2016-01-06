package org.igye.learnpl2.controllers

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.KeyCode._
import javafx.scene.layout.StackPane

import org.apache.logging.log4j.{LogManager, Logger}
import org.igye.jfxutils.Implicits.nodeToNodeOps
import org.igye.jfxutils.action.{Action, Shortcut}
import org.igye.jfxutils.annotations.FxmlFile
import org.igye.jfxutils.fxml.{FxmlSupport, Initable}
import org.igye.jfxutils.{JfxUtils, Window}

@FxmlFile("fxml/MainWindow.fxml")
class MainWindowController extends Window with Initable {
    implicit val log: Logger = LogManager.getLogger()

    @FXML
    protected var rootNode: StackPane = _
    @FXML
    protected var textBtn: Button = _
    @FXML
    protected var cardsBtn: Button = _

    private val textsController = FxmlSupport.load[MainWindowTextsController]

    private val textAction = new Action {
        override val description: String = "Enter text mode"
        setShortcut(Shortcut(T))
        override protected def onAction(): Unit = {
            setEnabled(false)
            rootNode.getChildren.add(textsController.rootPane)
            textsController.rootPane.focus()
        }
    }

    private val actions = List(
        textAction
    )

    override def init(): Unit = {
        require(rootNode != null)
        require(textBtn != null)
        require(cardsBtn != null)

        initWindow(rootNode)

        textsController.onCloseHandler = () => {
            rootNode.getChildren.removeAll(textsController.rootPane)
            textAction.setEnabled(true)
        }

        Action.bind(textAction, textBtn)
        JfxUtils.bindActionsToSceneProp(rootNode.sceneProperty(), actions)
    }
}
