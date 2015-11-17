package org.igye.learnpl2.controllers

import org.igye.commonutils.Enum

case class State(name: String)
object State extends Enum[State] {
    val NOT_LOADED = addElem(State("NOT_LOADED"))
    val ONLY_TEXT = addElem(State("ONLY_TEXT"))
    val TEXT_WITH_INPUTS = addElem(State("TEXT_WITH_INPUTS"))
}