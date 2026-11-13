package com.otaku.wings.data.remote

import com.otaku.wings.data.model.Chapter
import com.otaku.wings.data.model.MangaPage
import com.otaku.wings.data.model.Series
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AxorafyClient {
    private val http = OkHttpClient()

    suspend fun latest(): List<Series> = query(orderBy = "lastChapterAddedAt")

    suspend fun popular(): List<Series> = query(orderBy = "totalViews")

    suspend fun search(keyword: String): List<Series> = query(searchTerm = keyword)

    suspend fun details(seriesUrl: String): Pair<Series?, List<Chapter>> = withContext(Dispatchers.IO) {
        val slug = slugFromSeriesUrl(seriesUrl)
        if (slug.isBlank()) return@withContext null to emptyList()

        val root = getJson("/api/post") {
            addQueryParameter("postSlug", slug)
        }
        val post = root.optJSONObject("post") ?: return@withContext null to emptyList()
        val series = parseSeries(post)
        series to parseChapters(post.optJSONArray("chapters"), series.sourceUrl, series.id)
    }

    suspend fun chapters(seriesUrl: String): List<Chapter> = details(seriesUrl).second

    suspend fun pages(chapterUrl: String): List<MangaPage> = withContext(Dispatchers.IO) {
        val chapterId = chapterUrl.substringAfterLast('#').trim()
        if (chapterId.isBlank() || chapterId == chapterUrl) return@withContext emptyList()

        val root = getJson("/api/chapter") {
            addQueryParameter("chapterId", chapterId)
        }
        val chapter = root.optJSONObject("chapter") ?: return@withContext emptyList()
        if (chapter.optBoolean("isShortLinkLocked") || chapter.optBoolean("isLockedByCoins")) {
            throw IOException("هذا الفصل مقفل ويتطلب فتحه من موقع Axorafy")
        }
        if (chapter.optBoolean("isPermanentlyLocked") || chapter.optBoolean("isLocked")) {
            throw IOException("هذا الفصل مقفل حاليًا")
        }

        val images = chapter.optJSONArray("images") ?: JSONArray()
        val parsed = buildList {
            for (index in 0 until images.length()) {
                val image = images.optJSONObject(index) ?: continue
                val url = image.stringValue("url")
                if (url.isNotBlank()) {
                    add((image.optInt("order", Int.MAX_VALUE)) to url.replace(" ", "%20"))
                }
            }
        }.sortedBy { it.first }

        parsed.mapIndexed { index, (_, url) -> MangaPage(index = index, imageUrl = url) }
    }

    private suspend fun query(
        searchTerm: String = "",
        orderBy: String = "lastChapterAddedAt"
    ): List<Series> = withContext(Dispatchers.IO) {
        val root = getJson("/api/query") {
            addQueryParameter("perPage", "18")
            addQueryParameter("searchTerm", searchTerm.trim())
            addQueryParameter("orderBy", orderBy)
            addQueryParameter("orderDirection", "desc")
            addQueryParameter("page", "1")
        }
        val posts = root.optJSONArray("posts") ?: JSONArray()
        buildList {
            for (index in 0 until posts.length()) {
                val post = posts.optJSONObject(index) ?: continue
                if (post.optBoolean("isNovel")) continue
                add(parseSeries(post))
            }
        }
    }

    private fun parseSeries(post: JSONObject): Series {
        val id = post.optInt("id", 0).toString()
        val slug = post.stringValue("slug")
        val sourceUrl = "$WEB_URL/series/$slug#$id"
        val chapters = post.optJSONArray("chapters")
        val latestChapter = chapters?.optJSONObject(0)?.let { chapter ->
            val number = chapter.stringValue("number")
            val title = chapter.stringValue("title")
            listOf("الفصل $number", title).filter { it.isNotBlank() }.joinToString(" ")
        }.orEmpty()

        return Series(
            id = id,
            title = post.stringValue("postTitle"),
            author = post.stringValue("author").ifBlank { post.stringValue("artist") },
            coverUrl = post.stringValue("featuredImage"),
            synopsis = post.stringValue("postContent").stripHtml(),
            genre = post.optJSONArray("genres").toGenreText(),
            latestChapter = latestChapter,
            sourceUrl = sourceUrl
        )
    }

    private fun parseChapters(array: JSONArray?, seriesUrl: String, seriesId: String): List<Chapter> {
        if (array == null) return emptyList()
        val slug = seriesUrl.substringAfter("/series/").substringBefore('#')
        return buildList {
            for (index in 0 until array.length()) {
                val chapter = array.optJSONObject(index) ?: continue
                val id = chapter.optInt("id", 0).toString()
                val chapterSlug = chapter.stringValue("slug")
                val number = chapter.stringValue("number")
                val title = chapter.stringValue("title")
                val url = "$WEB_URL/series/$slug/$chapterSlug#$id"
                add(
                    Chapter(
                        id = id,
                        seriesId = seriesId,
                        number = number,
                        title = title,
                        date = chapter.stringValue("createdAt"),
                        url = url,
                        isRead = false
                    )
                )
            }
        }
    }

    private suspend fun getJson(
        path: String,
        configure: okhttp3.HttpUrl.Builder.() -> Unit = {}
    ): JSONObject {
        val url = "$API_URL$path".toHttpUrl().newBuilder().apply(configure).build()
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Referer", "$WEB_URL/")
            .header("Origin", "$WEB_URL/")
            .get()
            .build()
        return withContext(Dispatchers.IO) {
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Axorafy API: HTTP ${response.code}")
                JSONObject(response.body?.string().orEmpty())
            }
        }
    }

    private fun slugFromSeriesUrl(url: String): String =
        url.substringAfter("/series/", "").substringBefore('#').substringBefore('/').trim()

    private fun JSONArray?.toGenreText(): String {
        if (this == null) return ""
        return buildList {
            for (index in 0 until length()) {
                val genre = optJSONObject(index)?.stringValue("name").orEmpty()
                if (genre.isNotBlank()) add(genre)
            }
        }.distinct().joinToString("، ")
    }

    private fun JSONObject.stringValue(key: String): String =
        if (has(key) && !isNull(key)) optString(key).trim() else ""

    private fun String.stripHtml(): String =
        replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()

    private companion object {
        const val WEB_URL = "https://azorafly.com"
        const val API_URL = "https://api.azorafly.com"
        const val USER_AGENT = "Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
    }
}
