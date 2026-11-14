package com.otaku.wings.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChapterDisplayTitleTest {

    private fun chapter(title: String, number: String = "7") = Chapter(
        id = "c",
        seriesId = "s",
        number = number,
        title = title,
        url = "u"
    )

    @Test
    fun `series name is stripped from the chapter title`() {
        assertEquals(
            "الفصل 7",
            chapter("حكاية السيف - الفصل 7").displayTitle("حكاية السيف")
        )
    }

    @Test
    fun `separators left at the edges are trimmed`() {
        assertEquals(
            "الفصل 3",
            chapter("ظل المدينة – الفصل 3 |").displayTitle("ظل المدينة")
        )
    }

    @Test
    fun `blank title falls back to the chapter number`() {
        assertEquals("الفصل 12", chapter("", number = "12").displayTitle("أي قصة"))
    }

    @Test
    fun `blank series title leaves the title untouched`() {
        assertEquals("الفصل 5", chapter("الفصل 5").displayTitle(""))
    }

    @Test
    fun `title with diacritics is preserved`() {
        assertEquals(
            "الفَصْلُ الأوّل",
            chapter("الفَصْلُ الأوّل").displayTitle("قصة")
        )
    }
}
