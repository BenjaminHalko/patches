// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.thumbnails

import android.net.Uri

internal data class DecodedThumbnailUrl(
    val sanitizedUrl: String,
    val urlPath: String,
    val videoId: String,
    val imageQuality: String,
    val imageExtension: String,
    val viewTrackingParameters: String,
) {
    // Other ytimg hosts often lack stills; WEBP stills also fail or load slower than JPG.
    fun createStillsUrl(quality: ThumbnailQuality): String =
        "https://i.ytimg.com/$urlPath/$videoId/${quality.altImageNameToUse}.$imageExtension"
}

private val thumbnailPaths = listOf("/vi/", "/vi_webp/")

internal fun decodeThumbnailUrl(url: String): DecodedThumbnailUrl? {
    val uri = Uri.parse(url)
    val host = uri.host ?: return null
    if (host != "ytimg.com" && !host.endsWith(".ytimg.com")) return null
    val path = uri.path ?: return null
    if (thumbnailPaths.none(path::startsWith)) return null
    val pathStart = url.indexOf('/', "https://".length) + 1
    if (pathStart <= 0) return null
    val pathEnd = url.indexOf('/', pathStart)
    if (pathEnd < 0) return null
    val videoIdStart = pathEnd + 1
    val videoIdEnd = url.indexOf('/', videoIdStart)
    if (videoIdEnd < 0) return null
    val qualityStart = videoIdEnd + 1
    val qualityEnd = url.indexOf('.', qualityStart)
    if (qualityEnd < 0) return null
    val queryStart = url.indexOf('?', qualityEnd).let { if (it < 0) url.length else it }
    return DecodedThumbnailUrl(
        url.substring(0, queryStart),
        url.substring(pathStart, pathEnd),
        url.substring(videoIdStart, videoIdEnd),
        url.substring(qualityStart, qualityEnd),
        url.substring(qualityEnd + 1, queryStart),
        url.substring(queryStart),
    )
}
