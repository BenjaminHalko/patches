// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.text.Spanned
import app.reseam.youtube.dislike.requests.RYDVoteData
import app.reseam.youtube.settings.VideoSettings

/** Guarded by the owning fetch's monitor. */
internal class DislikeSpanCache {
    private data class Entry(
        val source: Spanned,
        val replacement: Spanned,
        val configuration: Configuration,
    )

    private data class Configuration(
        val layout: SpanLayout,
        val compact: Boolean = VideoSettings.rydCompactLayout.value,
        val percentage: Boolean = VideoSettings.rydDislikePercentage.value,
        val estimatedLikes: Boolean = VideoSettings.rydEstimatedLike.value,
        val dark: Boolean = isDarkModeEnabled,
    )

    private var entry: Entry? = null

    fun clear() {
        entry = null
    }

    fun update(original: Spanned, layout: SpanLayout, votes: RYDVoteData): Spanned {
        val configuration = Configuration(layout)
        val cached = entry
        if (
            cached != null &&
                cached.configuration == configuration &&
                haveEqualTextAndColor(original, cached.source)
        ) {
            return cached.replacement
        }
        // Preserve the previous replacement when the host retries a source whose rendering failed.
        entry = entry?.copy(source = original)
        return createDislikeSpan(original, layout, votes).also {
            entry = Entry(original, it, configuration)
        }
    }
}

internal enum class SpanLayout(val segmented: Boolean, val rolling: Boolean) {
    DISLIKES(false, false),
    ROLLING_DISLIKES(false, true),
    SEGMENTED(true, false),
    ROLLING_SEGMENTED(true, true),
}

internal fun regularSpanLayout(segmented: Boolean, rolling: Boolean): SpanLayout =
    when {
        segmented && rolling -> SpanLayout.ROLLING_SEGMENTED
        segmented -> SpanLayout.SEGMENTED
        rolling -> SpanLayout.ROLLING_DISLIKES
        else -> SpanLayout.DISLIKES
    }
