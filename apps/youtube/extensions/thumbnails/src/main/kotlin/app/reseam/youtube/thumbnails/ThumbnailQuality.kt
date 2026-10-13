// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.thumbnails

import app.reseam.youtube.settings.AppearanceSettings

internal enum class ThumbnailStillTime(val altImageNumber: Int) {
    BEGINNING(1),
    MIDDLE(2),
    END(3),
}

internal enum class ThumbnailQuality(val originalName: String, val altImageName: String) {
    // Ordered from lowest to highest resolution.
    DEFAULT("default", ""),
    MQDEFAULT("mqdefault", "mq"),
    HQDEFAULT("hqdefault", "hq"),
    SDDEFAULT("sddefault", "sd"),
    HQ720("hq720", "hq720_"),
    MAXRESDEFAULT("maxresdefault", "maxres");

    val altImageNameToUse: String
        get() = altImageName + thumbnailStillTime().altImageNumber
}

private val originalNameToEnum =
    ThumbnailQuality.entries
        .flatMap { quality ->
            listOf(quality.originalName to quality) +
                ThumbnailStillTime.entries.map {
                    "${quality.originalName}_custom_${it.altImageNumber}" to quality
                }
        }
        .toMap()
private val altNameToEnum =
    ThumbnailQuality.entries
        .flatMap { quality ->
            ThumbnailStillTime.entries.map { quality.altImageName + it.altImageNumber to quality }
        }
        .toMap()

internal fun altImageQuality(altImageName: String): ThumbnailQuality? = altNameToEnum[altImageName]

internal fun thumbnailQualityToUse(originalSize: String): ThumbnailQuality? {
    val quality = originalNameToEnum[originalSize] ?: return null
    val fast = AppearanceSettings.altThumbnailStillsFast.value
    // SD stills have washed-out colors; 720 does not. Fast mode stops at SD.
    return when (quality) {
        ThumbnailQuality.SDDEFAULT,
        ThumbnailQuality.HQ720 -> if (fast) ThumbnailQuality.SDDEFAULT else ThumbnailQuality.HQ720
        ThumbnailQuality.MAXRESDEFAULT ->
            if (fast) ThumbnailQuality.SDDEFAULT else ThumbnailQuality.MAXRESDEFAULT
        else -> quality
    }
}

internal fun thumbnailStillTime(): ThumbnailStillTime =
    ThumbnailStillTime.entries.firstOrNull {
        it.name == AppearanceSettings.altThumbnailStillsTime.value
    } ?: ThumbnailStillTime.MIDDLE
