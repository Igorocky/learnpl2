package org.igye.learnpl2

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
        val text = "Word1 word2. Word3 word4 word5. Word6.\r\nWord7.\nWord8\r"

        val sentences = TextFunctions.splitTextOnSentences(text)
        Assert.assertEquals("Word1 word2.", sentences(0))
        Assert.assertEquals(" Word3 word4 word5.", sentences(1))
        Assert.assertEquals(" Word6.", sentences(2))
        Assert.assertEquals("\r\nWord7.", sentences(3))
        Assert.assertEquals("\nWord8\r", sentences(4))
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
    def splitSentenceOnPartsTest2(): Unit = {
        var sentence = "\r\nWo\r\nrd1\r\n"

        var parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\r\n", parts(0))
        Assert.assertEquals("Wo", parts(1))
        Assert.assertEquals("\r\n", parts(2))
        Assert.assertEquals("rd1", parts(3))
        Assert.assertEquals("\r\n", parts(4))

        sentence = "\nWo\nrd1\n"

        parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\n", parts(0))
        Assert.assertEquals("Wo", parts(1))
        Assert.assertEquals("\n", parts(2))
        Assert.assertEquals("rd1", parts(3))
        Assert.assertEquals("\n", parts(4))

        sentence = "\n\nWo\r\n\r\nrd1\r\n"

        parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\n\n", parts(0))
        Assert.assertEquals("\r\n\r\n", parts(2))
    }

    @Test
    def splitSentenceOnPartsTest3(): Unit = {
        var sentence = "\n\n— "

        var parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertEquals("\n\n", parts(0))
        Assert.assertEquals("—", parts(1))
        Assert.assertEquals(" ", parts(2))
    }

    @Test
    def checkUserInputTest(): Unit = {
        Assert.assertTrue(TextFunctions.checkUserInput("abc", "abc", None))
        Assert.assertFalse(TextFunctions.checkUserInput("abc", "mnk", None))
    }
}
