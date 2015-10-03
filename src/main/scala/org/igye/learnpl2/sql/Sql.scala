package org.igye.learnpl2.sql

import scala.collection.mutable.ListBuffer

case class TableAlias(table: Table, alias: String) extends HasFields {
    override def name: String = alias

    override def fields: List[Field] = table.fields
}

abstract class Sql extends SqlImplicits{
    private val tables = ListBuffer[TableAlias]()
    private var whereExpr: Option[BoolExpr] = None

    protected def T[TT <: Table](table: TT, aliasStr: String): TT = {
        tables += TableAlias(table, aliasStr)
        table
    }

    protected def where(expr: BoolExpr): Unit = {
        whereExpr = Some(expr)
    }

    def text = {
        s"select * from $listOfTablesAndJoins${whereExpr.map(" where " + _.text).getOrElse("")}"
    }

    protected def listOfTablesAndJoins = {
        tables.map(t => t.table.name + " " + t.alias).mkString(", ")
    }
}



