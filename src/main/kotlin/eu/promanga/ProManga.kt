package eu.promanga

import eu.kanade.tachiyomi.source.model.*
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Element

class ProManga : HttpSource() {
    override val name = "ProManga"
    override val baseUrl = "https://promanga.net"
    override val lang = "ar"
    override val supportsLatest = true

    override fun popularMangaRequest(page: Int): Request =
        GET("$baseUrl/series?page=$page")

    override fun popularMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val mangas = document.select("div.series-card a").map {
            SManga.create().apply {
                title = it.select("h3").text()
                setUrlWithoutDomain(it.attr("href"))
            }
        }
        val hasNextPage = document.select("a.next").isNotEmpty()
        return MangasPage(mangas, hasNextPage)
    }

    override fun latestUpdatesRequest(page: Int) = popularMangaRequest(page)
    override fun latestUpdatesParse(response: Response) = popularMangaParse(response)

    override fun mangaDetailsParse(response: Response): SManga {
        val document = response.asJsoup()
        return SManga.create().apply {
            title = document.select("h1").text()
            description = document.select("div.description").text()
        }
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        return document.select("ul.chapter-list a").map {
            SChapter.create().apply {
                name = it.text()
                setUrlWithoutDomain(it.attr("href"))
            }
        }
    }

    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()
        return document.select("img.page-image").mapIndexed { i, el: Element ->
            Page(i, "", el.attr("src"))
        }
    }

    override fun imageUrlParse(response: Response): String = ""
}
