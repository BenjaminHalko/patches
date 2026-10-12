// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtube.core

import app.reseam.patch.appEntry
import app.reseam.patch.before
import app.reseam.patch.settings.section
import app.reseam.patch.settings.settingsHost
import app.reseam.patches.bundle.reseamBrand
import app.reseam.patches.youtubecommon.YouTubeCommonSettings
import app.reseam.patches.youtubecommon.YouTubeContext

val youTubeSettings = settingsHost("youtube") {
    compatibleWith(YOUTUBE)
    brand(reseamBrand)

    // Diagnostics belongs to the host rather than a feature patch.
    settings(section(YouTubeSettingsPages.Advanced, "Diagnostics", YouTubeCommonSettings.debugLogging))

    install {
        // Extensions log and look up resources through this context from their first hook on.
        appEntry { call(YouTubeContext.init, application) }
    }
}
