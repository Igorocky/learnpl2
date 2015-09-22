package org.igye.learnpl2

import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.text.Text

import scala.collection.mutable.ListBuffer

//Ja mieszkam w Krakowie, a pan? Gdzie pan mieszka?
class SentenceForTextFlow(sentence: String) {
    private val words = sentence.split("\\b")

    def getText = {
        new Text(sentence)
    }

    def getNodes:List[Node] = {
        var i = 0;
        words.map(w => {i += 1; if (i == 4 || i == 6) new TextField() else new Text(words(i - 1))}).toList
    }
}
