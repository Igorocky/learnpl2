package org.igye.learnpl2

import java.lang.Thread.UncaughtExceptionHandler
import javafx.application.Application
import javafx.stage.Stage

import org.apache.logging.log4j.LogManager
import org.igye.jfxutils.fxml.FxmlSupport
import org.igye.learnpl2.controllers.{MainWindowController, MainWindowTextsController}
import org.igye.learnpl2.settings.Settings

object Main {
    def main(args: Array[String]) {
        Settings.loadAppSettings()
        Settings.loadUserSettings()
        Application.launch(classOf[App], args: _*)
    }
}

class App  extends Application {
    implicit val log = LogManager.getLogger()

    override def start(primaryStage: Stage): Unit = {
        val prevUncaughtExceptionHandler =  Thread.currentThread().getUncaughtExceptionHandler
        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler {
            override def uncaughtException(t: Thread, e: Throwable): Unit = {
                log.error(e.getMessage, e)
                if (prevUncaughtExceptionHandler != null) {
                    prevUncaughtExceptionHandler.uncaughtException(t, e)
                } else {
                    throw e
                }
            }
        })
        val mainWindow = FxmlSupport.load[MainWindowController](primaryStage)
        mainWindow.stage.setTitle("learnpl2")
        mainWindow.stage.setMaximized(true)
        mainWindow.open()
    }
}