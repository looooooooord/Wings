package com.otaku.wings.ui.detail

import com.otaku.wings.data.model.Chapter

/**
 * What the detail screen's primary button should do.
 *
 * @param chapter the chapter to open (null when there are no chapters at all)
 * @param isContinue true when the series has a last-read chapter that still exists in the list
 */
data class PrimaryAction(
    val chapter: Chapter?,
    val isContinue: Boolean
)

/**
 * Resolves the primary action from data that already exists on the device.
 *
 * Chapters arrive newest-first, so the oldest chapter is the last element.
 * When the stored last-read chapter no longer exists (source deleted it), we fall back to the
 * oldest chapter so the button never opens something unexpected.
 */
internal fun resolvePrimaryAction(
    chapters: List<Chapter>,
    lastReadChapterUrl: String?
): PrimaryAction {
    val lastRead = lastReadChapterUrl?.let { url -> chapters.firstOrNull { it.url == url } }
    return when {
        lastRead != null -> PrimaryAction(chapter = lastRead, isContinue = true)
        chapters.isNotEmpty() -> PrimaryAction(chapter = chapters.last(), isContinue = false)
        else -> PrimaryAction(chapter = null, isContinue = false)
    }
}
