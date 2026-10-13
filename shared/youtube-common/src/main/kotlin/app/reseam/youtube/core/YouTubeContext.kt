// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.core

import android.content.Context
import app.reseam.bundle.Logger
import app.reseam.runtime.Export

object YouTubeContext {
    @Volatile private var context: Context? = null

    @Export
    @JvmStatic
    fun init(applicationContext: Context) {
        context = applicationContext
        Logger.info { "Reseam extensions loaded" }
    }

    val applicationContext: Context
        get() = context ?: error("YouTubeContext.init has not run")
}
