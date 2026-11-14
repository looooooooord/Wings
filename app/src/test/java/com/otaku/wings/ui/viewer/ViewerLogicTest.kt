package com.otaku.wings.ui.viewer

import com.otaku.wings.data.local.ReadingDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewerLogicTest {

    @Test
    fun `single page mode returns one spread per page`() {
        assertEquals(
            listOf(listOf(0), listOf(1), listOf(2)),
            buildSpreads(pageCount = 3, twoPageMode = false, firstPageSingle = true)
        )
    }

    @Test
    fun `zero pages returns no spreads`() {
        assertTrue(buildSpreads(0, twoPageMode = true, firstPageSingle = true).isEmpty())
    }

    @Test
    fun `one page returns a single spread`() {
        assertEquals(
            listOf(listOf(0)),
            buildSpreads(1, twoPageMode = true, firstPageSingle = true)
        )
    }

    @Test
    fun `two pages with first page single`() {
        assertEquals(
            listOf(listOf(0), listOf(1)),
            buildSpreads(2, twoPageMode = true, firstPageSingle = true)
        )
    }

    @Test
    fun `two pages without first page single pair up`() {
        assertEquals(
            listOf(listOf(0, 1)),
            buildSpreads(2, twoPageMode = true, firstPageSingle = false)
        )
    }

    @Test
    fun `even page count without first page single`() {
        assertEquals(
            listOf(listOf(0, 1), listOf(2, 3)),
            buildSpreads(4, twoPageMode = true, firstPageSingle = false)
        )
    }

    @Test
    fun `odd page count leaves a trailing single page`() {
        assertEquals(
            listOf(listOf(0), listOf(1, 2), listOf(3, 4)),
            buildSpreads(5, twoPageMode = true, firstPageSingle = true)
        )
        assertEquals(
            listOf(listOf(0, 1), listOf(2, 3), listOf(4)),
            buildSpreads(5, twoPageMode = true, firstPageSingle = false)
        )
    }

    @Test
    fun `rtl reading reverses the pager and puts the first page on the right`() {
        assertTrue(reverseLayoutFor(ReadingDirection.RTL))
        assertEquals(listOf(2, 1), orderedSpread(listOf(1, 2), ReadingDirection.RTL))
    }

    @Test
    fun `ltr reading keeps natural order`() {
        assertFalse(reverseLayoutFor(ReadingDirection.LTR))
        assertEquals(listOf(1, 2), orderedSpread(listOf(1, 2), ReadingDirection.LTR))
    }

    @Test
    fun `spread index lookup maps page to its spread`() {
        val spreads = buildSpreads(5, twoPageMode = true, firstPageSingle = true)
        assertEquals(0, spreadIndexForPage(spreads, 0))
        assertEquals(1, spreadIndexForPage(spreads, 2))
        assertEquals(2, spreadIndexForPage(spreads, 4))
        assertEquals(0, spreadIndexForPage(spreads, 99))
    }

    @Test
    fun `page clamping keeps values in range`() {
        assertEquals(0, clampPage(-5, 10))
        assertEquals(9, clampPage(50, 10))
        assertEquals(0, clampPage(3, 0))
    }
}
