package org.igye.learnpl2.sql

import scala.language.experimental.macros
import scala.reflect.internal.Trees
import scala.reflect.macros.blackbox
import scala.reflect.macros.blackbox.Context

class SqlMacro(val c: Context) {

    import c.universe._

    def sqlApply(sql: c.Tree) = {
        val tree = sql
        val str = tree.toString()
        println(s"%^%^%^str = $str")
        tree
    }
}
