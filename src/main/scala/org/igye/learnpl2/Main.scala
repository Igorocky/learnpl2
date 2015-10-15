package org.igye.learnpl2

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.StackPane
import javafx.stage.Stage

import org.igye.jfxutils.action.{Shortcut, Action}
import org.igye.jfxutils.{JfxUtils, nodeToHasEvens, FxmlSupport, JfxFuture}
import org.igye.learnpl2.controllers.MainWindowController
import org.slf4j.LoggerFactory

object Main {
    def main(args: Array[String]) {
        Application.launch(classOf[App], args: _*);
    }
}

class App  extends Application {
    implicit val log = LoggerFactory.getLogger(this.getClass)

    override def start(primaryStage: Stage): Unit = {
        JfxFuture.setJfxThread(Thread.currentThread())
        val mainWindow = FxmlSupport.load[MainWindowController]
        mainWindow.primaryStage = primaryStage

        val scene = new Scene(mainWindow.getMainWindow)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Using TextFlow")
        primaryStage.setMaximized(true)
        primaryStage.show()
    }

    /*override def start(primaryStage: Stage): Unit = {
        JfxFuture.setJfxThread(Thread.currentThread())
        val anchorPaneController = FxmlSupport.load[AnchorPaneTestController]("fxml/AnchorPaneTest.fxml")
        val printToConsoleAction = new Action {
            setShortcut(new Shortcut(List(KeyCode.CONTROL, KeyCode.P)))
            override protected[this] def onAction(): Unit = {
                println("printToConsoleAction was triggered.")
            }
            override val description: String = "Print to console"
        }
        var printingIsEnabled = true
        val enableDisablePrintingAction = new Action {
            setShortcut(new Shortcut(List(KeyCode.CONTROL, KeyCode.E)))
            override protected[this] def onAction(): Unit = {
                printingIsEnabled = !printingIsEnabled
                printToConsoleAction.setEnabled(printingIsEnabled)
            }
            override val description: String = "Enable/disable printing"
        }
        Action.bind(enableDisablePrintingAction, anchorPaneController.getButton1)
        Action.bind(printToConsoleAction, anchorPaneController.getButton2)
        JfxUtils.bindShortcutActionTrigger(
            anchorPaneController.getAnchorPane,
            List(
                enableDisablePrintingAction,
                printToConsoleAction
            )
        )
        val scene = new Scene(anchorPaneController.getAnchorPane)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Learn PL")
        primaryStage.setMaximized(true)
        primaryStage.show()
    }*/
}