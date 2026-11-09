package com.panomc.plugins.linkredirect.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import java.net.URI

/**
 * Keeps redirect data harmless for visitors: the target is only ever an http(s) URL or a path on this site, and the
 * custom page HTML is cleaned of scripts, event handlers and other active content.
 */
object RedirectSafety {
    private val safelist: Safelist = Safelist.relaxed()
        .addTags("hr", "center", "font", "figure", "figcaption", "mark", "s", "del", "ins")
        .addAttributes(":all", "style", "class", "title", "align", "dir")
        .addAttributes("a", "target", "rel")
        .addAttributes("font", "color", "size", "face")
        .preserveRelativeLinks(true)

    private val outputSettings = Document.OutputSettings().prettyPrint(false)

    fun isSafeTargetUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        // Browsers drop tabs and new lines inside URLs, so a value with control characters is never accepted.
        if (url.any { it.isISOControl() } || url != url.trim()) {
            return false
        }

        // A path on this site. "//host" and "/\host" are treated by browsers as another host, so they go through
        // the absolute URL check below.
        if (url.startsWith("/") && !url.startsWith("//") && !url.startsWith("/\\")) {
            return true
        }

        val uri = try {
            URI(url)
        } catch (_: Exception) {
            return false
        }

        val scheme = uri.scheme?.lowercase() ?: return false

        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }

    fun sanitizeHtml(html: String?): String? {
        if (html == null) {
            return null
        }

        return Jsoup.clean(html, "http://localhost/", safelist, outputSettings)
    }
}
