// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.bundle

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

fun Resources.dpToPx(value: Number, rounded: Boolean = true): Int {
    val pixels = value.toFloat() * displayMetrics.density
    return if (rounded) Math.round(pixels) else pixels.toInt()
}

fun Resources.isNightMode(): Boolean =
    configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/** The app's own resource [name] of [type], or 0 when the app has none. */
fun Context.identifier(name: String, type: String): Int =
    resources.getIdentifier(name, type, packageName)
