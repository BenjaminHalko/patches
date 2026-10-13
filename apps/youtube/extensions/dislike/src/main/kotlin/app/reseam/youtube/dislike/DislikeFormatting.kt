// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike

import android.icu.text.CompactDecimalFormat
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.NumberFormat
import android.os.Build
import java.util.Locale

private val countFormatter by lazy {
    val locale = Locale.getDefault()
    CompactDecimalFormat.getInstance(locale, CompactDecimalFormat.CompactStyle.SHORT).apply {
        // YouTube uses English digits even when the locale uses another numeral system.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            decimalFormatSymbols = englishDigits(locale)
    }
}
private val percentageFormatter by lazy {
    val locale = Locale.getDefault()
    NumberFormat.getPercentInstance(locale).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && this is DecimalFormat) {
            decimalFormatSymbols = englishDigits(locale)
        }
    }
}

internal fun formatCount(count: Long): String =
    synchronized(ReturnYouTubeDislike::class.java) {
        countFormatter.format(count)
    }

internal fun formatPercentage(percentage: Float): String =
    synchronized(ReturnYouTubeDislike::class.java) {
        percentageFormatter.maximumFractionDigits = if (percentage.toDouble() >= 0.01) 0 else 1
        percentageFormatter.format(percentage.toDouble())
    }

private fun englishDigits(locale: Locale): DecimalFormatSymbols =
    DecimalFormatSymbols.getInstance(locale).apply {
        digitStrings = DecimalFormatSymbols.getInstance(Locale.ENGLISH).digitStrings
    }
