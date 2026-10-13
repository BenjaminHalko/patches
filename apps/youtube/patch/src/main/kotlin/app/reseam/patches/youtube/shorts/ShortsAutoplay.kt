// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtube.shorts

import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.section
import app.reseam.patches.youtube.core.YOUTUBE
import app.reseam.patches.youtube.core.YouTubeSettingsPages
import app.reseam.patches.youtube.core.mainActivityOnCreate
import app.reseam.patches.youtube.core.youTubeSettings
import app.reseam.youtube.settings.ShortsSettings
import app.reseam.youtube.settings.YouTubeStrings
import app.reseam.youtube.shorts.bindings.ShortsAutoplay

private val repeatBehavior = klass("Shorts repeat behavior") {
    strings("REEL_LOOP_BEHAVIOR_AUTO_ADVANCE")
}

private val decodeRepeatBehavior = method("decode Shorts repeat behavior") {
    inClass(repeatBehavior)
    params(Type.Int)
    returns(repeatBehavior.descriptor)
}

private val shortsRepeatPolicy = method("Shorts repeat policy") {
    calls(decodeRepeatBehavior)
    returns(repeatBehavior.descriptor)
}

val shortsAutoplay = patch("Shorts autoplay") {
    description("Automatically plays the next Short when Shorts autoplay is enabled.")
    compatibleWith(YOUTUBE)
    dependsOn(youTubeSettings)
    settings(youTubeSettings, section(YouTubeSettingsPages.Shorts, YouTubeStrings.sectionShorts, ShortsSettings.shortsAutoplay, ShortsSettings.shortsAutoplayBackground))

    execute {
        mainActivityOnCreate.before { ShortsAutoplay.setMainActivity(thisObject) }
        shortsRepeatPolicy.after {
            capture("result").assign(
                ShortsAutoplay.changeRepeatBehavior(capture("result")).cast(repeatBehavior.descriptor),
            )
        }
    }
}
