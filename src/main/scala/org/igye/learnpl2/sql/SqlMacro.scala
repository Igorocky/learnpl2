package org.igye.learnpl2.sql

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

class SqlMacro(val c: Context) {
    private val log = org.slf4j.LoggerFactory.getLogger(this.getClass)

    import c.universe._

    def sqlApply(sql: c.Tree) = {
        println("===================================================")
        println(s"%^%^%^sql = ${sql.toString()}")
        println("===================================================")


        val methodTWithOneArg = c.universe.typeOf[SqlClass].members.find(decl =>
            decl.name.toString == "T"
                && decl.isMethod
                && decl.asMethod.paramLists.nonEmpty
                && decl.asMethod.paramLists(0).size == 1
        ).get
        val methodTWithTwoArgs = c.universe.typeOf[SqlClass].decls.find(decl =>
            decl.name.toString == "T2"
                && decl.isMethod
                && decl.asMethod.paramLists.nonEmpty
                && decl.asMethod.paramLists(0).size == 2
        ).get

        val newTree = new Transformer {
            override def transform(tree: c.Tree): c.Tree = tree match {
                case ValDef(
                    modifiers,
                    valName,
                    tree1,
                    Apply(
                        TypeApply(
                            select @ Select(qual @ qualifier, methodName),
                            argsOfTypeApply
                        ),
                        argsOfApply
                    )
                ) =>
                    if (select.symbol == methodTWithOneArg) {
                        log.info("FOUND!!!!")
//                        tree.asInstanceOf[ValDef].rhs.asInstanceOf[Apply]
//                            .fun.asInstanceOf[TypeApply].fun.asInstanceOf[Select].symbol.
//                        val res = ValDef(
//                            modifiers,
//                            valName,
//                            tree1,
//                            Apply(
//                                TypeApply(
//                                    Select(qual, methodTWithOneArg),
////                                    Ident(methodTWithTwoArgs),
//                                    argsOfTypeApply
//                                ),
////                                (Literal(Constant(valName.toString.trim)) :: argsOfApply.reverse).reverse
//                                argsOfApply
//                            )
//                        )
//                        val res = q"val ${TermName(valName.toString.trim)} = $methodTWithTwoArgs(..$argsOfApply, ${valName.toString.trim})"
                        val res = q"val ${TermName(valName.toString.trim)} = ${TermName("T3")}(..$argsOfApply, ${valName.toString.trim})"
                        println("++++++++++++++++++++++++++++")
                        println(s"%^%^%^res = ${res.toString()}")
                        println("++++++++++++++++++++++++++++")
//                        val resChecked = c.typecheck(res)
//                        resChecked
                        res
//                        tree
                    } else {
                        tree
                    }
                case _ => super.transform(tree)
            }
        }.transform(sql)

        newTree
    }
}
