// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import app.reseam.bundle.dpToPx
import app.reseam.youtube.core.YouTubeContext

internal const val MIDDLE_SEPARATOR_CHARACTER = '◎'
internal val leftSeparatorBounds =
    Rect(
        0,
        0,
        YouTubeContext.applicationContext.resources.dpToPx(1.2f),
        YouTubeContext.applicationContext.resources.dpToPx(14f),
    )
private val middleSeparatorSize = YouTubeContext.applicationContext.resources.dpToPx(3.7f)
internal val middleSeparatorBounds = Rect(0, 0, middleSeparatorSize, middleSeparatorSize)
internal val leftSeparatorShapePaddingPixels =
    YouTubeContext.applicationContext.resources.dpToPx(8.4f)
private val leftSeparatorShape = ShapeDrawable(RectShape()).apply { bounds = leftSeparatorBounds }
internal val separatorColor: Int
    get() = if (isDarkModeEnabled) 0x33FFFFFF else 0xFFD9D9D9.toInt()
internal val leftSeparatorDrawable: ShapeDrawable
    get() = leftSeparatorShape.apply { paint.color = separatorColor }

internal fun isPreviouslyCreatedSegmentedSpan(text: String): Boolean =
    MIDDLE_SEPARATOR_CHARACTER in text
