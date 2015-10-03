package org.igye.learnpl2.sql

import scala.language.experimental.macros
import scala.reflect.internal.Trees
import scala.reflect.macros.blackbox
import scala.reflect.macros.blackbox.Context

class SqlMacro(val c: Context) {
    private val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

    import c.universe._

    def sqlApply(sql: c.Tree) = {
        val methodTWithOneArg = c.universe.typeOf[SqlClass].members.find(decl =>
            decl.name.toString == "T"
                && decl.isMethod
                && decl.asMethod.paramLists.nonEmpty
                && decl.asMethod.paramLists(0).size == 1
        ).get
        val methodTWithTwoArgs = c.universe.typeOf[SqlClass].decls.find(decl =>
            decl.name.toString == "T"
                && decl.isMethod
                && decl.asMethod.paramLists.nonEmpty
                && decl.asMethod.paramLists(0).size == 2
        ).get

        new Traverser {
            override def traverse(tree: c.Tree): Unit = tree match {
                case ValDef(
                    modifiers,
                    termName,
                    tree1,
                    Apply(
                        TypeApply(
                            select /*@ Select(qualifier, TermName("T"))*/,
                            argsOfTypeApply
                        ),
                        argsOfApply
                    )
                ) =>
                    if (select.symbol == methodTWithOneArg) {
                        log.info("FOUND!!!!")
                    }
                    log.info(s"termName = ${termName.toString()}")
                    log.info(s"select = ${select.toString()}")
                case _ => super.traverse(tree)
            }
        }.traverse(sql)


        val tree = sql
        val str = tree.toString()
        println(s"%^%^%^str = $str")
        tree
    }
}
