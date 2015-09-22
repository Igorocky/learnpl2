package org.igye.learnpl2.utils

import scala.collection.mutable.ListBuffer

trait Enum[T] {
    private val values = ListBuffer[T]()
    private var allValues_ : Option[List[T]] = None

    protected def field(v: T): T = {
        values += v
        v
    }

    def allValues = {
        if (allValues_.isEmpty) {
            allValues_ = Option(values.toList)
        }
        allValues_.get
    }
}
