package com.otaku.wings.ui.viewer

import com.otaku.wings.data.local.ReadingDirection

/**
 * Groups original page indices into what one manga screen shows.
 * Unchanged behaviour from the original implementation: with two-page mode off every page is
 * its own spread; with it on pages pair up, optionally leaving the first (cover) page alone.
 */
internal fun buildSpreads(
    pageCount: Int,
    twoPageMode: Boolean,
    firstPageSingle: Boolean
): List<List<Int>> {
    if (!twoPageMode || pageCount == 0) return (0 until pageCount).map { listOf(it) }
    val spreads = mutableListOf<List<Int>>()
    var i = 0
    if (firstPageSingle) {
        spreads.add(listOf(0))
        i = 1
    }
    while (i < pageCount) {
        spreads.add(if (i + 1 < pageCount) listOf(i, i + 1) else listOf(i))
        i += 2
    }
    return spreads
}

/**
 * Horizontal pager behaviour.
 *
 * The pager is always laid out with an LTR layout direction so page order does not depend on the
 * app's RTL UI. With reverseLayout the pager index increases towards the left, which is the manga
 * convention (the next page appears on the left).
 *
 * setting RTL  -> reverseLayout = true  -> next page on the left  (manga)
 * setting LTR  -> reverseLayout = false -> next page on the right (western comics)
 */
internal fun reverseLayoutFor(direction: ReadingDirection): Boolean =
    direction == ReadingDirection.RTL

/**
 * Physical left-to-right order of the pages inside one spread.
 * The spread Row is rendered LTR, so the returned list is exactly left to right.
 *
 * RTL reading: page 1 sits on the right of page 2.
 * LTR reading: page 1 sits on the left of page 2.
 */
internal fun orderedSpread(spread: List<Int>, direction: ReadingDirection): List<Int> =
    when (direction) {
        ReadingDirection.RTL -> spread.reversed()
        ReadingDirection.LTR -> spread
    }

/** Index of the spread that contains [pageIndex], or 0 when not found. */
internal fun spreadIndexForPage(spreads: List<List<Int>>, pageIndex: Int): Int {
    val index = spreads.indexOfFirst { pageIndex in it }
    return if (index >= 0) index else 0
}

/** Clamps a requested page into the valid range. */
internal fun clampPage(page: Int, pageCount: Int): Int =
    if (pageCount <= 0) 0 else page.coerceIn(0, pageCount - 1)
