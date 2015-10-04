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
    def splitSentenceOnPartsTest(): Unit = {
        val sentence = "Word1 word2, \"word3\" - word4-suff (word5, word6!): word7; word8."

        val parts = TextFunctions.splitSentenceOnParts(sentence)
        Assert.assertTrue(parts.contains("Word1"))
        Assert.assertTrue(parts.contains("word2"))
        Assert.assertTrue(parts.contains("word3"))
        Assert.assertTrue(parts.contains("word4-suff"))
        Assert.assertTrue(parts.contains("word5"))
        Assert.assertTrue(parts.contains("word6"))
        Assert.assertTrue(parts.contains("word7"))
        Assert.assertTrue(parts.contains("word8"))
    }

    @Test
    def checkUserInputTest(): Unit = {
        Assert.assertTrue(TextFunctions.checkUserInput("abc", "abc", None))
        Assert.assertFalse(TextFunctions.checkUserInput("abc", "mnk", None))
    }
}
