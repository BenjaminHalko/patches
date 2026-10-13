// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.thumbnails

import android.net.Uri
import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug
import app.reseam.youtube.core.showToast
import app.reseam.youtube.settings.AppearanceSettings
import app.reseam.youtube.settings.YouTubeStrings

private const val DEARROW_FAILURE_API_BACKOFF_MILLISECONDS = 5 * 60 * 1000L

internal class DeArrowApi {
    @Volatile private var resumeAt = 0L
    private val apiUri = validateSettings()
    private val apiPrefix =
        "${apiUri.scheme}://${apiUri.host}${if (apiUri.port == -1) "" else ":${apiUri.port}"}/"

    init {
        Logger.debug { "Using DeArrow API address: $apiPrefix" }
    }

    private fun validateSettings(): Uri {
        val uri = Uri.parse(AppearanceSettings.altThumbnailDearrowApiUrl.value)
        // Unsecured HTTP does not start requests or trigger the callback hooks.
        if (uri.scheme != "https" || uri.host == null) {
            showToast(YouTubeStrings.alternativeThumbnailsInvalidApiUrl)
            return Uri.parse(AppearanceSettings.altThumbnailDearrowApiUrl.default)
        }
        return uri
    }

    fun thumbnailUrl(videoId: String, fallbackUrl: String): String =
        apiUri
            .buildUpon()
            .appendQueryParameter("videoID", videoId)
            .appendQueryParameter("redirectUrl", fallbackUrl)
            .build()
            .toString()

    fun ownsUrl(imageUrl: String): Boolean = imageUrl.startsWith(apiPrefix)

    fun canUse(): Boolean =
        when {
            resumeAt == 0L -> true
            resumeAt < System.currentTimeMillis() -> {
                resumeAt = 0
                true
            }
            else -> false
        }

    fun handleError(url: String, statusCode: Int) {
        Logger.debug { "Encountered DeArrow error.  URL: $url" }
        val now = System.currentTimeMillis()
        if (resumeAt < now) {
            resumeAt = now + DEARROW_FAILURE_API_BACKOFF_MILLISECONDS
            if (AppearanceSettings.altThumbnailDearrowConnectionToast.value) {
                showToast(
                    if (statusCode != 0) YouTubeStrings.alternativeThumbnailsHttpError(statusCode)
                    else YouTubeStrings.alternativeThumbnailsConnectionFailed
                )
            }
        }
    }
}
