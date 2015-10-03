package org.igye.learnpl2.sql

import scala.collection.mutable.ListBuffer

import scala.language.experimental.macros

case class TableAlias(table: Table, alias: String) extends HasFields {
    override def name: String = alias

    override def fields: List[Field] = table.fields
}

abstract class SqlClass extends SqlImplicits {
    private val tables = ListBuffer[TableAlias]()
    private var whereExpr: Option[BoolExpr] = None

    def T2[TT <: Table](table: TT, aliasStr: String): TT = {
        tables += TableAlias(table, aliasStr)
        table
    }

    def T[TT <: Table](table: TT): TT = {
        throw new IllegalStateException("this method should not be used in runtime")
        table
    }

    def where(expr: BoolExpr): Unit = {
        whereExpr = Some(expr)
    }

    def text = {
        s"select * from $listOfTablesAndJoins${whereExpr.map(" where " + _.text).getOrElse("")}"
    }

    protected def listOfTablesAndJoins = {
        tables.map(t => t.table.name + " " + t.alias).mkString(", ")
    }
}

object SqlObj extends SqlImplicits {
    def apply(sql: SqlClass): SqlClass = macro SqlMacro.sqlApply
}