// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import app.reseam.bundle.Logger
import app.reseam.youtube.core.debug
import app.reseam.youtube.dislike.requests.RYDVoteData
import app.reseam.youtube.settings.VideoSettings
import app.reseam.youtube.settings.YouTubeStrings

internal fun createLikesSpan(original: Spanned, votes: RYDVoteData): Spanned {
    if (!containsNumber(original)) {
        if (!VideoSettings.rydEstimatedLike.value) {
            Logger.debug { "Likes are hidden" }
            return original
        }
        Logger.debug { "Using estimated likes" }
    }
    return withStyling(original, formatCount(votes.likeCount))
}

internal fun createDislikeSpan(
    original: Spanned,
    layout: SpanLayout,
    votes: RYDVoteData,
): SpannableString {
    if (!layout.segmented) return withDislikes(original, votes)
    val likes: CharSequence =
        when {
            containsNumber(original) -> original
            !VideoSettings.rydEstimatedLike.value ->
                return withStyling(original, YouTubeStrings.rydLikesHidden)
            else -> {
                Logger.debug { "Using estimated likes" }
                formatCount(votes.likeCount)
            }
        }
    val compact = VideoSettings.rydCompactLayout.value
    return SpannableString(
        SpannableStringBuilder().apply {
            if (!compact) append(leftSeparator(layout.rolling))
            append(withStyling(original, likes))
            append(middleSeparator(compact, layout.rolling))
            append(withDislikes(original, votes))
        }
    )
}

private fun leftSeparator(rolling: Boolean): SpannableString {
    val direction = textDirectionString
    if (rolling) return SpannableString(direction)
    return SpannableString("$direction  ").apply {
        setSpan(
            VerticallyCenteredImageSpan(leftSeparatorDrawable, false),
            1,
            2,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
        )
        setSpan(
            FixedWidthEmptySpan(leftSeparatorShapePaddingPixels),
            2,
            3,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
        )
    }
}

private fun middleSeparator(compact: Boolean, rolling: Boolean): SpannableString {
    val marker = MIDDLE_SEPARATOR_CHARACTER
    val text = if (compact) "  $marker  " else " \u2009\u2009$marker\u2009\u2009 "
    val index = text.length / 2
    val drawable =
        ShapeDrawable(OvalShape()).apply {
            paint.color = separatorColor
            bounds = middleSeparatorBounds
        }
    // Rolling Number's styled span must occupy the same width as the measured string.
    return SpannableString(text).apply {
        setSpan(
            VerticallyCenteredImageSpan(drawable, rolling),
            index,
            index + 1,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
        )
    }
}

internal fun haveEqualTextAndColor(one: Spanned, two: Spanned): Boolean {
    if (one.toString() != two.toString()) return false
    val oneColors = one.getSpans(0, one.length, ForegroundColorSpan::class.java)
    val twoColors = two.getSpans(0, two.length, ForegroundColorSpan::class.java)
    return oneColors.size == twoColors.size &&
        oneColors.zip(twoColors).all { (first, second) ->
            first.foregroundColor == second.foregroundColor
        }
}

private fun withDislikes(source: Spanned, votes: RYDVoteData): SpannableString =
    withStyling(
        source,
        if (VideoSettings.rydDislikePercentage.value) formatPercentage(votes.dislikePercentage)
        else formatCount(votes.dislikeCount),
    )

internal fun withStyling(source: Spanned, text: CharSequence): SpannableString {
    if (source === text && source is SpannableString) return source
    return SpannableString(text).apply {
        source.getSpans(0, source.length, Any::class.java).forEach { span ->
            setSpan(span, 0, length, source.getSpanFlags(span))
        }
    }
}
