package org.igye.learnpl2.sql

import org.igye.learnpl2.sql.SqlObj._
import org.junit.{Assert, Test}

class SqlTest {
    @Test
    def test1(): Unit = {
        val t1 = new Table("TABLE1") {
            val f1 = field("f1")
            val f2 = field("f2")
        }

        val t2 = new Table("TABLE2") {
            val f3 = field("f3")
            val f4 = field("f4")
        }

        val sql = new SqlClass {
            val t = T2(t1, "t")
            val tt = T2(t2, "tt")
            where {
                fieldOfTableAlias("t", t.f1) == fieldOfTableAlias("tt", tt.f3)
            }
        }

        //        println(sql.text)
        Assert.assertEquals("select * from TABLE1 t, TABLE2 tt where t.f1 = tt.f3", sql.text)
    }
}
