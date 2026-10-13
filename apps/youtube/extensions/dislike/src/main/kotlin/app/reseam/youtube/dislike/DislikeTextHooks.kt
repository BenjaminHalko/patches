// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

@file:JvmName("DislikeTextHooks")

package app.reseam.youtube.dislike

import android.text.SpannableString
import android.text.Spanned
import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.settings.VideoSettings

internal enum class LithoTextMode {
    STANDARD,
    ROLLING,
}

@Export
fun onLithoTextLoaded(path: StringBuilder?, original: CharSequence): CharSequence =
    replaceLithoText(path, original, LithoTextMode.STANDARD)

internal fun replaceLithoText(
    path: StringBuilder?,
    original: CharSequence,
    mode: LithoTextMode,
): CharSequence {
    try {
        if (path == null || !VideoSettings.rydEnabled.value) return original
        if (mode == LithoTextMode.ROLLING && "video_action_bar.e" !in path) return original
        if ("segmented_like_dislike_button.e" in path) {
            val data = currentVideoData ?: return original
            return data.getDislikesSpanForRegularVideo(
                original as? Spanned ?: SpannableString(original),
                true,
                mode == LithoTextMode.ROLLING,
            )
        }
        if (mode == LithoTextMode.ROLLING) return original
        val button =
            ShortsButton.entries.firstOrNull { it.paths.any { part -> part in path } }
                ?: return original
        if (button.replacesVisibleCount || !containsNumber(original)) {
            return button.replace(original)
        }
        decrementUseLithoDataIfNeeded()
    } catch (ex: Exception) {
        Logger.error(ex) { "onLithoTextLoaded failure" }
    }
    return original
}
