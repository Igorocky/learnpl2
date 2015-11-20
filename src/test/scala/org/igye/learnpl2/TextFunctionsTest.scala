package org.igye.learnpl2

import org.igye.learnpl2.TextFunctions.GeneralCaseInsensitiveStringFilter
import org.junit.{Assert, Test}

class TextFunctionsTest {
    @Test
    def canWordBeHiddenTest(): Unit = {
        Assert.assertTrue(TextFunctions.isHiddable("abc"))
        Assert.assertFalse(TextFunctions.isHiddable(","))
        Assert.assertFalse(TextFunctions.isHiddable(", "))
        Assert.assertFalse(TextFunctions.isHiddable("– "))
        Assert.assertFalse(TextFunctions.isHiddable("\""))
        Assert.assertFalse(TextFunctions.isHiddable(": ["))
        Assert.assertFalse(TextFunctions.isHiddable("] ( "))
        Assert.assertFalse(TextFunctions.isHiddable(")) "))
        Assert.assertFalse(TextFunctions.isHiddable(" / "))
        Assert.assertFalse(TextFunctions.isHiddable(" \\"))
        Assert.assertFalse(TextFunctions.isHiddable("; "))
    }

    @Test
    def splitTextOnSentencesTest(): Unit = {
        val text = "Word1 word2. Word3 word4 word5. Word6."

        val sentences = TextFunctions.splitTextOnSentences(text)
        Assert.assertEquals("Word1 word2.", sentences(0))
        Assert.assertEquals("Word3 word4 word5.", sentences(1))
        Assert.assertEquals("Word6.", sentences(2))
    }

    @Test
    def splitSentenceOnPartsTest1(): Unit = {
        val sentence = "Word1 word2, \"word3\" - word4-suff (word5, word6!): word7; word8." +
            " WordWith'Apostrophe. WordWith–LongHyphen."

        val parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertTrue(parts.contains("Word1"))
        Assert.assertTrue(parts.contains("word2"))
        Assert.assertTrue(parts.contains("word3"))
        Assert.assertTrue(parts.contains("word4-suff"))
        Assert.assertTrue(parts.contains("word5"))
        Assert.assertTrue(parts.contains("word6"))
        Assert.assertTrue(parts.contains("word7"))
        Assert.assertTrue(parts.contains("word8"))
        Assert.assertTrue(parts.contains("WordWith'Apostrophe"))
        Assert.assertTrue(parts.contains("WordWith–LongHyphen"))
    }

    @Test
    def checkUserInputTest(): Unit = {
        Assert.assertTrue(TextFunctions.checkUserInput("abc", "abc", None))
        Assert.assertFalse(TextFunctions.checkUserInput("abc", "mnk", None))
    }

    @Test
    def extractPathAndFilterTest(): Unit = {
        var res = TextFunctions.extractPathAndFilter("C:/dir1/fil")
        Assert.assertEquals("C:/dir1/", res.path)
        Assert.assertEquals("fil", res.filter)

        res = TextFunctions.extractPathAndFilter("C:/dir1/")
        Assert.assertEquals("C:/dir1/", res.path)
        Assert.assertEquals("", res.filter)

        res = TextFunctions.extractPathAndFilter("C:")
        Assert.assertEquals("", res.path)
        Assert.assertEquals("C:", res.filter)

        res = TextFunctions.extractPathAndFilter("")
        Assert.assertEquals("", res.path)
        Assert.assertEquals("", res.filter)
    }

    @Test
    def generalCaseInsensitiveStringFilterTest(): Unit = {
        Assert.assertTrue(GeneralCaseInsensitiveStringFilter("bd").matches("abcde"))
        Assert.assertTrue(GeneralCaseInsensitiveStringFilter("bd").matches("ABCDE"))
        Assert.assertFalse(GeneralCaseInsensitiveStringFilter("db").matches("abcde"))
        Assert.assertTrue(GeneralCaseInsensitiveStringFilter("").matches("abcde"))
        Assert.assertFalse(GeneralCaseInsensitiveStringFilter("abc").matches("abbde"))
    }
}
