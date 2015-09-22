package org.igye.learnpl2

import java.net.URL
import javafx.application.Application
import javafx.event.{ActionEvent, EventHandler}
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.{Button, Tab, TabPane, TextField}
import javafx.scene.layout.{HBox, Pane, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.{Text, TextFlow}
import javafx.stage.Stage

import org.igye.jfxutils.{JfxActionEventHandler, JfxUtils}
import org.igye.learnpl2.twotextedits.TwoTextEdits

object Main {
    def main(args: Array[String]) {
        Application.launch(classOf[App], args: _*);
    }
}

class App  extends Application {

    override def start(primaryStage: Stage): Unit = {
//        val fxmlUrl = this.getClass().getClassLoader().getResource("fxml/TwoTextEdits.fxml")
//        val loader = new FXMLLoader()
//        loader.setLocation(fxmlUrl)
//        val root = loader.load[HBox]()
        val twoTextEdits = new TwoTextEdits
        twoTextEdits.onEnterPressedInFirstEditHnd = Some((e: ActionEvent) => println(s"e=$e"))

        val scene = new Scene(twoTextEdits.root)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Using TextFlow")
//        primaryStage.setMaximized(true)
        primaryStage.show()
    }
}