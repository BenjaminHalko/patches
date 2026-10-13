// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.core

import android.widget.Toast
import app.reseam.bundle.showToast

fun showToast(message: String, duration: Int = Toast.LENGTH_LONG) =
    YouTubeContext.applicationContext.showToast(message, duration)
