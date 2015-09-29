package org.igye.learnpl2

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

import org.igye.jfxutils.{FxmlSupport, JfxFuture}
import org.igye.learnpl2.twotextedits.TwoTextEdits
import org.slf4j.LoggerFactory

object Main {
    def main(args: Array[String]) {
        Application.launch(classOf[App], args: _*);
    }
}

class App  extends Application {
    implicit val log = LoggerFactory.getLogger(this.getClass)

    override def start(primaryStage: Stage): Unit = {
        val twoTextEdits = new TwoTextEdits
        JfxFuture.setJfxThread(Thread.currentThread())
        val mainWindow = FxmlSupport.load[MainWindowController]("fxml/MainWindow.fxml")
        mainWindow.primaryStage = primaryStage

        val scene = new Scene(mainWindow.mainWindow)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Using TextFlow")
//        primaryStage.setMaximized(true)
        primaryStage.show()
    }
}