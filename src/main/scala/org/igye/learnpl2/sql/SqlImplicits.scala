package org.igye.learnpl2.sql

trait SqlImplicits {
    def fieldOfTableAlias(aliasStr: String, field: Field): RefToField = {
        new RefToField(new FieldAlias(new TableAlias(field.table, aliasStr), field))
    }
}