package org.igye.learnpl2

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

import org.igye.commonutils.LogFailureFuture
import org.igye.jfxutils.JfxActionEventHandler
import org.igye.learnpl2.twotextedits.TwoTextEdits
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

object Main {
    def main(args: Array[String]) {
        Application.launch(classOf[App], args: _*);
    }
}

class App  extends Application {
    implicit val log = LoggerFactory.getLogger(this.getClass)

    override def start(primaryStage: Stage): Unit = {
        val twoTextEdits = new TwoTextEdits
        twoTextEdits.onEnterPressedInFirstEditHnd = Some(JfxActionEventHandler{e =>
            LogFailureFuture {
                println(s"e=$e")
            }
        })

        val scene = new Scene(twoTextEdits.root)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Using TextFlow")
//        primaryStage.setMaximized(true)
        primaryStage.show()
    }
}