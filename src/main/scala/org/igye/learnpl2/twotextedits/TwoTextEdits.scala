package org.igye.learnpl2.twotextedits

import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.layout.HBox

class TwoTextEdits {
    private val (root_, controller) = loadRoot()

    def root = root_

    var onEnterPressedInFirstEditHnd: Option[ActionEvent => Unit] = _

    private def loadRoot() = {
        val fxmlUrl = this.getClass().getClassLoader().getResource("fxml/TwoTextEdits.fxml")
        val loader = new FXMLLoader()
        loader.setLocation(fxmlUrl)
        (loader.load[HBox](), loader.getController[TwoTextEditsController].bind(this))
    }
}

