// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.tiktok.ads

import app.reseam.patch.Type
import app.reseam.patch.dex.AccessFlags
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patches.tiktok.core.AWEME
import app.reseam.patches.tiktok.core.TIKTOK
import app.reseam.patches.tiktok.core.TikTokSettingsPages
import app.reseam.patches.tiktok.core.tiktokSettings
import app.reseam.patches.tiktok.feed.feedFilter
import app.reseam.patches.tiktok.feed.hidePosts
import app.reseam.tiktok.settings.FeedSettings
import app.reseam.tiktok.settings.TikTokStrings

val hideAds =
    patch("Hide ads") {
        description(
            "Removes ads and promoted posts from TikTok's feeds, and the full-screen ad at app start."
        )
        compatibleWith(TIKTOK)
        dependsOn(feedFilter)
        settings(
            tiktokSettings,
            section(TikTokSettingsPages.Feed, TikTokStrings.sectionAds, FeedSettings.hideAds),
        )

        execute {
            hidePosts(FeedSettings.hideAds, "ADS")
            gate(FeedSettings.hideAds) {
                midFeedAd.alwaysReturnNull()
                splashAdsEnabled.alwaysReturn(false)
            }
        }
    }

private val midFeedAd =
    klass("com.ss.android.ugc.aweme.commercialize.feed.assem.midad.MidAdResponse").method(
        "getAweme"
    ) {
        returns(AWEME)
    }

// TikTok's own switch for the full-screen ad at app start, which then continues as the first post.
private val splashAdsEnabled =
    method("splashAdsEnabled") {
        strings("ab_repo_cold_boot", "splash_ad_enable")
        flags(AccessFlags.STATIC)
        params()
        returns(Type.Boolean)
    }
