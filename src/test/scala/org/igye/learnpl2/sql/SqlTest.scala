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

        val sql = SqlObj ({
            val t = SqlObj.T(t1, "t")
            val tt = SqlObj.T(t2, "tt")
            SqlObj.where {
                SqlObj.fieldOfTableAlias("t", t.f1) == Sql.fieldOfTableAlias("tt", tt.f3)
            }
        })

//        val sql = {
//            final class $anon extends _root_.org.igye.learnpl2.sql.Sql {
//                val t: org.igye.learnpl2.sql.Table{val f1: org.igye.learnpl2.sql.Field; val f2: org.igye.learnpl2.sql.Field} = Sql.T[org.igye.learnpl2.sql.Table{val f1: org.igye.learnpl2.sql.Field; val f2: org.igye.learnpl2.sql.Field}](t1, "t");
//                val tt: org.igye.learnpl2.sql.Table{val f3: org.igye.learnpl2.sql.Field; val f4: org.igye.learnpl2.sql.Field} = Sql.T[org.igye.learnpl2.sql.Table{val f3: org.igye.learnpl2.sql.Field; val f4: org.igye.learnpl2.sql.Field}](t2, "tt")
//            };
//            new $anon()
//        }

        //        println(sql.text)
        Assert.assertEquals("select * from TABLE1 t, TABLE2 tt where t.f1 = tt.f3", sql.text)
    }
}
