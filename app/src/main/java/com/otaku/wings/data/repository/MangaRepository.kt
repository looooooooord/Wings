package com.otaku.wings.data.repository

import android.content.Context
import com.otaku.wings.data.local.AppDatabase
import com.otaku.wings.data.local.entity.BookmarkEntity
import com.otaku.wings.data.local.entity.ReadHistoryEntity
import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.MangaPage
import com.otaku.wings.data.model.Series
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MangaRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val bookmarkDao = db.bookmarkDao()
    private val historyDao = db.readHistoryDao()

    // مصدر المحتوى الخارجي محذوف؛ تبقى البيانات المحلية للمحفوظات وسجل القراءة.

    suspend fun fetchLatest(): List<Series> = emptyList()

    suspend fun fetchPopular(): List<Series> = emptyList()

    suspend fun fetchChapters(seriesUrl: String): List<Chapter> = emptyList()

    suspend fun fetchSeriesDetail(url: String): Pair<Series?, List<Chapter>> = null to emptyList()

    suspend fun fetchPages(chapterUrl: String): List<MangaPage> = emptyList()

    suspend fun search(keyword: String): List<Series> = emptyList()

    // ── المحفوظات ─────────────────────────────────────────

    fun getBookmarks(): Flow<List<Series>> =
        bookmarkDao.getAll().map { list ->
            list.map { e ->
                Series(
                    id = e.seriesId,
                    title = e.title,
                    author = e.author,
                    coverUrl = e.coverUrl,
                    sourceUrl = e.sourceUrl
                )
            }
        }

    fun isBookmarked(seriesId: String): Flow<Boolean> =
        bookmarkDao.isBookmarked(seriesId)

    suspend fun addBookmark(series: Series) {
        bookmarkDao.insert(
            BookmarkEntity(
                seriesId = series.id,
                title = series.title,
                author = series.author,
                coverUrl = series.coverUrl,
                sourceUrl = series.sourceUrl
            )
        )
    }

    suspend fun removeBookmark(seriesId: String) {
        bookmarkDao.delete(seriesId)
    }

    // ── 읽기 기록 ───────────────────────────────────────

    fun getRecentHistory(limit: Int = 20): Flow<List<Series>> =
        historyDao.getRecent(limit).map { list ->
            list.distinctBy { it.seriesId }.map { e ->
                Series(
                    id = e.seriesId,
                    title = e.seriesTitle,
                    author = e.seriesAuthor,
                    coverUrl = e.coverUrl,
                    sourceUrl = e.seriesUrl,
                    latestChapter = e.chapterTitle
                )
            }
        }

    fun getLastReadChapterUrl(seriesId: String): Flow<String?> =
        historyDao.getLatestForSeries(seriesId).map { it?.chapterUrl }

    suspend fun removeHistory(seriesId: String) {
        historyDao.deleteBySeriesId(seriesId)
    }

    suspend fun recordHistory(
        chapterUrl: String,
        chapterTitle: String,
        seriesId: String,
        seriesUrl: String,
        seriesTitle: String,
        seriesAuthor: String,
        coverUrl: String,
        lastPage: Int,
        totalPages: Int
    ) {
        historyDao.insert(
            ReadHistoryEntity(
                chapterUrl = chapterUrl,
                seriesId = seriesId,
                seriesUrl = seriesUrl,
                seriesTitle = seriesTitle,
                seriesAuthor = seriesAuthor,
                coverUrl = coverUrl,
                chapterTitle = chapterTitle,
                lastPage = lastPage,
                totalPages = totalPages
            )
        )
    }

    suspend fun clearAllData() {
        bookmarkDao.clearAll()
        historyDao.clearAll()
    }

    // ── 백업/복원 ───────────────────────────────────────

    suspend fun exportBackupJson(): String {
        val bookmarks = bookmarkDao.getAll().first()
        val history = historyDao.getAll().first()

        val bookmarksArr = org.json.JSONArray()
        bookmarks.forEach { b ->
            bookmarksArr.put(
                org.json.JSONObject().apply {
                    put("seriesId", b.seriesId)
                    put("title", b.title)
                    put("author", b.author)
                    put("coverUrl", b.coverUrl)
                    put("sourceUrl", b.sourceUrl)
                    put("addedAt", b.addedAt)
                }
            )
        }
        val historyArr = org.json.JSONArray()
        history.forEach { h ->
            historyArr.put(
                org.json.JSONObject().apply {
                    put("chapterUrl", h.chapterUrl)
                    put("seriesId", h.seriesId)
                    put("seriesUrl", h.seriesUrl)
                    put("seriesTitle", h.seriesTitle)
                    put("seriesAuthor", h.seriesAuthor)
                    put("coverUrl", h.coverUrl)
                    put("chapterTitle", h.chapterTitle)
                    put("lastPage", h.lastPage)
                    put("totalPages", h.totalPages)
                    put("readAt", h.readAt)
                }
            )
        }
        return org.json.JSONObject().apply {
            put("bookmarks", bookmarksArr)
            put("history", historyArr)
        }.toString(2)
    }

    suspend fun importBackupJson(json: String) {
        val root = org.json.JSONObject(json)
        root.optJSONArray("bookmarks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                bookmarkDao.insert(
                    BookmarkEntity(
                        seriesId = o.getString("seriesId"),
                        title = o.optString("title"),
                        author = o.optString("author"),
                        coverUrl = o.optString("coverUrl"),
                        sourceUrl = o.optString("sourceUrl"),
                        addedAt = o.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        root.optJSONArray("history")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                historyDao.insert(
                    ReadHistoryEntity(
                        chapterUrl = o.getString("chapterUrl"),
                        seriesId = o.optString("seriesId"),
                        seriesUrl = o.optString("seriesUrl"),
                        seriesTitle = o.optString("seriesTitle"),
                        seriesAuthor = o.optString("seriesAuthor"),
                        coverUrl = o.optString("coverUrl"),
                        chapterTitle = o.optString("chapterTitle"),
                        lastPage = o.optInt("lastPage", 0),
                        totalPages = o.optInt("totalPages", 0),
                        readAt = o.optLong("readAt", System.currentTimeMillis())
                    )
                )
            }
        }
    }
}
