// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.instagram.feed

import app.reseam.instagram.feed.bindings.FollowingFeed
import app.reseam.instagram.settings.FeedSettings
import app.reseam.instagram.settings.InstagramStrings
import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patches.instagram.core.INSTAGRAM
import app.reseam.patches.instagram.core.instagramSettings
import app.reseam.patches.instagram.core.signatureCheck

val followingFeed = patch("Following-only feed") {
    description("Loads the home feed from accounts you follow, without suggested posts.")
    compatibleWith(INSTAGRAM)
    dependsOn(signatureCheck)
    settings(instagramSettings, section(InstagramStrings.sectionFeed, FeedSettings.followingOnly))

    execute {
        gate(FeedSettings.followingOnly) {
            klass(feedRequestParams.owner).method("<init>").after {
                thisObject.set(feedRequestParams, FollowingFeed.following(thisObject.field(feedRequestParams)))
            }
        }
    }
}

// Two request builders match; both read the same map.
private val feedRequestParams = method("feedTimelineRequest") {
    strings("feed/timeline/", "pagination_source")
    hasParam("kotlin.jvm.functions.Function1")
    first()
}.point { string("pagination_source") }
    .previous { field { type(Type.Map) } }
    .field("feedRequestParams")
