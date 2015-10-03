package org.igye.learnpl2.sql

import scala.collection.mutable.ListBuffer

class Field(val table: Table, val name: String)

trait HasFields {
    def name: String
    def fields: List[Field]
}

abstract class Table(val name: String) extends HasFields {
    private val fieldsBuff = ListBuffer[Field]()
    override lazy val fields = fieldsBuff.toList

    protected def field(name: String) = {
        fieldsBuff += new Field(this, name)
        fieldsBuff.last
    }
}