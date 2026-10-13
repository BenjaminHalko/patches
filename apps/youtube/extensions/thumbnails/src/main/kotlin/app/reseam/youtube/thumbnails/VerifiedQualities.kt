// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.thumbnails

import android.util.LruCache
import app.reseam.bundle.Logger
import app.reseam.bundle.isMainThread
import app.reseam.bundle.openHttp
import app.reseam.youtube.core.debug
import app.reseam.youtube.settings.AppearanceSettings
import java.io.IOException

private const val NOT_AVAILABLE_TIMEOUT_MILLISECONDS = 10 * 60 * 1000L
private val successfulImageStatuses = setOf(200, 206)
private val verifiedVideos = LruCache<String, VerifiedQualities>(1000)

private fun cachedQualities(videoId: String): VerifiedQualities? =
    synchronized(verifiedVideos) { verifiedVideos[videoId] }

private fun qualitiesFor(videoId: String): VerifiedQualities =
    synchronized(verifiedVideos) {
        verifiedVideos[videoId] ?: VerifiedQualities().also { verifiedVideos.put(videoId, it) }
    }

internal fun verifyAltThumbnailExists(
    videoId: String,
    quality: ThumbnailQuality,
    imageUrl: String,
): Boolean {
    // Never wait for a per-video monitor on the UI thread; a worker may hold it during IO.
    if (isMainThread()) return false
    val verified =
        if (AppearanceSettings.altThumbnailStillsFast.value) cachedQualities(videoId)
        else qualitiesFor(videoId)
    return verified?.verify(videoId, quality, imageUrl) ?: true
}

internal fun markAltThumbnailMissing(videoId: String, quality: ThumbnailQuality) =
    qualitiesFor(videoId).markMissing(videoId, quality)

private class VerifiedQualities {
    private data class Missing(val quality: ThumbnailQuality, val retryAt: Long)

    private var highest: ThumbnailQuality? = null
    private var missing: Missing? = null

    @Synchronized
    fun markMissing(videoId: String, quality: ThumbnailQuality) {
        val unavailable = missing
        if (unavailable == null || quality < unavailable.quality) {
            missing =
                Missing(quality, System.currentTimeMillis() + NOT_AVAILABLE_TIMEOUT_MILLISECONDS)
        }
        Logger.debug { "$quality not available for video: $videoId" }
    }

    @Synchronized
    fun verify(videoId: String, quality: ThumbnailQuality, imageUrl: String): Boolean {
        val available = highest
        if (available != null && available >= quality) return true
        val fast = AppearanceSettings.altThumbnailStillsFast.value
        val unavailable = missing
        if (unavailable != null && unavailable.quality <= quality) {
            if (fast || System.currentTimeMillis() < unavailable.retryAt) return false
            Logger.debug { "Resetting lowest verified quality for: $videoId" }
            missing = null
        }
        if (fast) return true
        val found =
            try {
                verifyImage(imageUrl)
            } catch (ex: IOException) {
                Logger.debug(ex) { "Could not verify alt URL: $imageUrl" }
                false
            }
        if (found) highest = maxOf(highest ?: quality, quality) else markMissing(videoId, quality)
        return found
    }
}

private fun verifyImage(url: String): Boolean =
    openHttp(url) {
        requestMethod = "HEAD"
        connectTimeout = 3000
        readTimeout = 3000
        setRequestProperty("Range", "bytes=0-0")
        val status = responseCode
        val type = contentType
        status in successfulImageStatuses && type?.startsWith("image/") == true
    }
