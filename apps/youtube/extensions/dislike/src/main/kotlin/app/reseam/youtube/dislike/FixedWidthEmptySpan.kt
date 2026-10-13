// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

internal class FixedWidthEmptySpan(private val fixedWidth: Int) : ReplacementSpan() {
    init {
        require(fixedWidth >= 0)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int = fixedWidth

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
    ) {}
}
