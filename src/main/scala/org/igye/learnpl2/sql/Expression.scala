package org.igye.learnpl2.sql

trait Expression {
    def text: String

    def ==(expr: Expression): EqEqExpr = {
        new EqEqExpr(this, expr)
    }
}

trait BoolExpr extends Expression

class EqEqExpr(val left: Expression, val right: Expression) extends BoolExpr {
    override def text: String = s"${left.text} = ${right.text}"
}

class RefToField(fieldAlias: FieldAlias) extends Expression {
    override def text: String = s"${fieldAlias.base.name}.${fieldAlias.field.name}"
}

class FieldAlias(val base: HasFields, val field: Field)