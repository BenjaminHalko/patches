// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

@file:JvmName("DislikeRollingNumbers")

package app.reseam.youtube.dislike

import android.view.View
import android.widget.TextView
import app.reseam.bundle.Logger
import app.reseam.runtime.Export
import app.reseam.youtube.core.debug
import app.reseam.youtube.settings.VideoSettings

@Export
fun onRollingNumberLoaded(path: StringBuilder?, original: String): String {
    try {
        val replacement = replaceLithoText(path, original, LithoTextMode.ROLLING)
        val text = replacement.toString()
        if (text != original) {
            rollingNumberSpan = replacement
            return text
        }
    } catch (ex: Exception) {
        Logger.error(ex) { "onRollingNumberLoaded failure" }
    }
    return original
}

@Export
fun onRollingNumberMeasured(text: String, measuredTextWidth: Float): Float {
    try {
        if (VideoSettings.rydEnabled.value && isPreviouslyCreatedSegmentedSpan(text)) {
            // The extra pixel avoids differing measurement/layout widths in some locales.
            return if (VideoSettings.rydCompactLayout.value) measuredTextWidth + 1
            else measuredTextWidth + 1 + leftSeparatorBounds.right + leftSeparatorShapePaddingPixels
        }
    } catch (ex: Exception) {
        Logger.error(ex) { "onRollingNumberMeasured failure" }
    }
    return measuredTextWidth
}

private fun addRollingNumberPatchChanges(view: TextView) {
    if (view.compoundDrawablePadding != 0) return
    view.compoundDrawablePadding = leftSeparatorShapePaddingPixels
    val separator = leftSeparatorDrawable
    if (isRightToLeftLocale) view.setCompoundDrawables(null, null, separator, null)
    else view.setCompoundDrawables(separator, null, null, null)
    view.textAlignment = View.TEXT_ALIGNMENT_CENTER
    view.setSingleLine(true)
}

private fun removeRollingNumberPatchChanges(view: TextView) {
    if (view.compoundDrawablePadding == 0) return
    view.compoundDrawablePadding = 0
    view.setCompoundDrawables(null, null, null, null)
    view.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
    view.setSingleLine(false)
}

@Export
fun updateRollingNumber(view: TextView, original: CharSequence): CharSequence {
    try {
        val isDislikeNumber =
            VideoSettings.rydEnabled.value && isPreviouslyCreatedSegmentedSpan(original.toString())
        val replacement =
            if (isDislikeNumber) {
                rollingNumberSpan.also {
                    if (it == null) Logger.debug { "Cannot update rolling number: no span loaded" }
                }
            } else null
        if (replacement == null) {
            removeRollingNumberPatchChanges(view)
            return original
        }
        if (VideoSettings.rydCompactLayout.value) removeRollingNumberPatchChanges(view)
        else addRollingNumberPatchChanges(view)
        view.setPadding(0, 0, 0, 0)
        return replacement
    } catch (ex: Exception) {
        Logger.error(ex) { "updateRollingNumber failure" }
        return original
    }
}
