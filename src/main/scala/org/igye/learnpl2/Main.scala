package org.igye.learnpl2

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

import org.apache.logging.log4j.LogManager
import org.igye.jfxutils.fxml.FxmlSupport
import org.igye.learnpl2.controllers.MainWindowController

object Main {
    def main(args: Array[String]) {
        Application.launch(classOf[App], args: _*)
    }
}

class App  extends Application {
    implicit val log = LogManager.getLogger()

    override def start(primaryStage: Stage): Unit = {
        val mainWindow = FxmlSupport.load[MainWindowController]

        val scene = new Scene(mainWindow.getMainWindow)
        primaryStage.setScene(scene)
        primaryStage.setTitle("learnpl2")
        primaryStage.setMaximized(true)
        primaryStage.show()
    }
}