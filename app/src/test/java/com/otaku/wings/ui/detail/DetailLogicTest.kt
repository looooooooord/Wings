package com.otaku.wings.ui.detail

import com.otaku.wings.data.model.Chapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailLogicTest {

    // Source lists arrive newest first.
    private val newestFirst = listOf(
        chapter("c3", 3),
        chapter("c2", 2),
        chapter("c1", 1)
    )

    @Test
    fun `continue reading is offered when the last read chapter still exists`() {
        val action = resolvePrimaryAction(newestFirst, "c2")
        assertTrue(action.isContinue)
        assertEquals("c2", action.chapter?.url)
    }

    @Test
    fun `start reading opens the oldest chapter when nothing was read`() {
        val action = resolvePrimaryAction(newestFirst, null)
        assertFalse(action.isContinue)
        assertEquals("c1", action.chapter?.url)
    }

    @Test
    fun `missing last read chapter falls back to the oldest chapter`() {
        val action = resolvePrimaryAction(newestFirst, "deleted-chapter")
        assertFalse(action.isContinue)
        assertEquals("c1", action.chapter?.url)
    }

    @Test
    fun `no chapters disables the primary action`() {
        val action = resolvePrimaryAction(emptyList(), "anything")
        assertNull(action.chapter)
        assertFalse(action.isContinue)
    }

    private fun chapter(url: String, number: Int) = Chapter(
        id = url,
        seriesId = "s1",
        number = number.toString(),
        title = "الفصل $number",
        url = url
    )
}
