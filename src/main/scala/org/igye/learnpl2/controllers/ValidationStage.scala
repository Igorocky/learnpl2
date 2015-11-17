package org.igye.learnpl2.controllers

import org.igye.commonutils.Enum

case class ValidationStage(name: String)
object ValidationStage extends Enum[ValidationStage] {
    val FILL_INPUTS = addElem(ValidationStage("FILL_INPUTS"))
    val CORRECTIONS_STAGE = addElem(ValidationStage("CORRECTIONS_STAGE"))
}