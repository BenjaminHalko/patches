// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.thumbnails

import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.core.debug
import app.reseam.youtube.navigation.NavigationBar
import app.reseam.youtube.navigation.NavigationBar.NavigationButton
import app.reseam.youtube.player.PlayerType
import app.reseam.youtube.settings.AppearanceSettings

internal enum class ThumbnailOption(val useDeArrow: Boolean, val useStillImages: Boolean) {
    ORIGINAL(false, false),
    DEARROW(true, false),
    DEARROW_STILL_IMAGES(true, true),
    STILL_IMAGES(false, true),
}

object AlternativeThumbnailsPatch {
    /** Added to the app's image request class by the patch. */
    @Export
    interface ImageRequest {
        @Export fun reseam_imageUrl(): String?
    }

    private val deArrow = DeArrowApi()

    private fun option(value: String): ThumbnailOption =
        ThumbnailOption.entries.firstOrNull { it.name == value } ?: ThumbnailOption.ORIGINAL

    private fun optionSettingForCurrentNavigation(): ThumbnailOption {
        // Search can remain active behind the player, and search can originate from any tab.
        if (PlayerType.current.isMaximizedOrFullscreen)
            return option(AppearanceSettings.altThumbnailPlayer.value)
        if (NavigationBar.isSearchBarActive)
            return option(AppearanceSettings.altThumbnailSearch.value)
        val home = option(AppearanceSettings.altThumbnailHome.value)
        val subscriptions = option(AppearanceSettings.altThumbnailSubscriptions.value)
        val library = option(AppearanceSettings.altThumbnailLibrary.value)
        if (home == subscriptions && home == library) return home
        return when (NavigationButton.selectedNavigationButton) {
            NavigationButton.SUBSCRIPTIONS,
            NavigationButton.NOTIFICATIONS -> subscriptions
            NavigationButton.LIBRARY -> library
            else -> home
        }
    }

    private fun verifiedStillUrl(decoded: DecodedThumbnailUrl, quality: ThumbnailQuality): String? {
        val replacement = decoded.createStillsUrl(quality)
        return replacement.takeIf { verifyAltThumbnailExists(decoded.videoId, quality, it) }
    }

    @Export
    @JvmStatic
    fun overrideImageURL(originalUrl: String): String {
        try {
            val option = optionSettingForCurrentNavigation()
            if (option == ThumbnailOption.ORIGINAL) return originalUrl
            val decoded = decodeThumbnailUrl(originalUrl) ?: return originalUrl
            val quality = thumbnailQualityToUse(decoded.imageQuality) ?: return originalUrl
            return when {
                option.useDeArrow && deArrow.canUse() -> {
                    val fallback =
                        if (option.useStillImages) verifiedStillUrl(decoded, quality) else null
                    deArrow.thumbnailUrl(decoded.videoId, fallback ?: decoded.sanitizedUrl)
                }
                option.useStillImages ->
                    verifiedStillUrl(decoded, quality)?.plus(decoded.viewTrackingParameters)
                        ?: originalUrl
                else -> originalUrl
            }
        } catch (ex: Exception) {
            Logger.error(ex) { "overrideImageURL failure" }
            return originalUrl
        }
    }

    @Export
    @JvmStatic
    fun onResponse(url: String, statusCode: Int) {
        try {
            if (statusCode == 200) return
            if (deArrow.ownsUrl(url)) {
                Logger.debug { "handleCronetSuccess, statusCode: $statusCode" }
                if (statusCode == 304) return
                deArrow.handleError(url, statusCode)
                return
            }
            if (statusCode == 404) {
                val decoded = decodeThumbnailUrl(url) ?: return
                Logger.debug { "handleCronetSuccess, image not available: ${decoded.sanitizedUrl}" }
                val quality = altImageQuality(decoded.imageQuality)
                if (quality == null) {
                    Logger.debug {
                        "Failed to recognize image quality of URL: ${decoded.sanitizedUrl}"
                    }
                    return
                }
                markAltThumbnailMissing(decoded.videoId, quality)
            }
        } catch (ex: Exception) {
            Logger.error(ex) { "Callback success error" }
        }
    }

    @Export
    @JvmStatic
    fun onFailure(request: Any?) {
        val imageRequest = request as? ImageRequest ?: return
        val url = imageRequest.reseam_imageUrl() ?: return
        if (deArrow.ownsUrl(url)) deArrow.handleError(url, 0)
    }
}
