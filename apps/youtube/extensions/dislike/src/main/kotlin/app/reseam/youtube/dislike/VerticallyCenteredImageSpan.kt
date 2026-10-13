// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

internal class VerticallyCenteredImageSpan(
    drawable: Drawable,
    private val useOriginalWidth: Boolean,
) : ImageSpan(drawable) {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int {
        val bounds = drawable.bounds
        fm?.apply {
            val metrics = paint.fontMetricsInt
            val halfDrawHeight = (bounds.bottom - bounds.top) / 2
            val center = metrics.ascent + (metrics.descent - metrics.ascent) / 2
            ascent = center - halfDrawHeight
            top = ascent
            bottom = center + halfDrawHeight
            descent = bottom
        }
        return if (useOriginalWidth) paint.measureText(text, start, end).toInt() else bounds.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint,
    ) {
        canvas.save()
        val metrics = paint.fontMetricsInt
        val center = y + metrics.descent - (metrics.descent - metrics.ascent) / 2
        val bounds = drawable.bounds
        val translateX =
            if (useOriginalWidth)
                x + (paint.measureText(text, start, end) - (bounds.right - bounds.left)) / 2
            else x
        val translateY = center - (bounds.bottom - bounds.top) / 2
        canvas.translate(translateX, translateY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}
