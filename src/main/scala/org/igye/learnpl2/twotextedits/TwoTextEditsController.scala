package org.igye.learnpl2.twotextedits

import javafx.event.ActionEvent
import javafx.fxml.FXML

class TwoTextEditsController {
    private var twoTextEdits: TwoTextEdits = _

    @FXML
    private def edit1OnAction(event: ActionEvent): Unit = {
        twoTextEdits.onEnterPressedInFirstEditHnd.foreach(_(event))
    }

    def bind(twoTextEdits: TwoTextEdits) = {
        this.twoTextEdits = twoTextEdits
        this
    }
}
