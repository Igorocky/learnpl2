package org.igye.learnpl2.controllers

import javafx.scene.control.TextField

import org.igye.learnpl2.models.Word

class EditElem(override val word: Word) extends TextField with WordRepr
