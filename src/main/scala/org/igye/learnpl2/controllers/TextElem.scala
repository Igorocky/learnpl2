package org.igye.learnpl2.controllers

import javafx.scene.text.Text

import org.igye.learnpl2.models.Word

class TextElem(override val word: Word) extends Text with WordRepr {
    setText(word.text)
}
