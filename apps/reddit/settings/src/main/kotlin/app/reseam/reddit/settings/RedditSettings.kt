// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.reddit.settings

import app.reseam.patch.settings.SettingsGroup
import app.reseam.patch.settings.toggle
import app.reseam.patches.bundle.lucide

object RedditSettings : SettingsGroup(RedditStrings) {
    val hideFeedAds by toggle(default = true, icon = lucide("megaphone-off"))
    val hideCommentAds by toggle(default = true, icon = lucide("message-square-off"))
    val hideTrendingUnits by toggle(default = true, icon = lucide("trending-up"))
    val hidePostCarousels by toggle(default = true, icon = lucide("gallery-horizontal"))
    val hidePromoUnits by toggle(default = true, icon = lucide("tag"))
    val hideCommunityUnits by toggle(default = false, icon = lucide("clipboard-list"))
    val hideContributionNudges by toggle(default = true, icon = lucide("pencil-off"))
    val hideAnswerCards by toggle(default = true, icon = lucide("bot-off"))
    val hideAwardBar by toggle(default = false, icon = lucide("award"))
    val hideRelatedPosts by toggle(default = true, icon = lucide("list-x"))
    val hideRecommendedPosts by toggle(default = false, icon = lucide("thumbs-down"))
    val hideRecommendedCommunities by toggle(default = true, icon = lucide("users"))
    val hideTrendingSearches by toggle(default = true, icon = lucide("search-x"))
    val ignoreSuggestedSort by toggle(default = false, icon = lucide("arrow-down-wide-narrow"))
    val hideCreateTab by toggle(default = true, icon = lucide("square-plus"))
    val hideInboxTab by toggle(default = false, icon = lucide("inbox"))
    val hidePremiumSettings by toggle(default = true, icon = lucide("settings"))
    val blockForcedUpdates by toggle(default = true, icon = lucide("shield-check"))
    val disableScreenshotBanner by toggle(default = true, icon = lucide("camera"))
    val skipNsfwWarning by toggle(default = false, icon = lucide("triangle-alert"))
    val blockAnalytics by toggle(default = true, icon = lucide("eye-off"))
    val blockAppsFlyer by toggle(default = true, icon = lucide("radar"))
    val disableAppRatePrompt by toggle(default = true, icon = lucide("star-off"))
    val hideAnswers by toggle(default = true, icon = lucide("message-circle-question"))
    val hideNotificationPrompts by toggle(default = true, icon = lucide("bell-off"))
    val hideUpdateNudges by toggle(default = true, icon = lucide("refresh-cw-off"))
    val hideShareNudges by toggle(default = true, icon = lucide("share-2"))
    val hideComposerPrompts by toggle(default = true, icon = lucide("lightbulb-off"))
    val hidePremiumUpsells by toggle(default = true, icon = lucide("gem"))
    val disableAdAutoplay by toggle(default = true, icon = lucide("circle-pause"))
    val hideSearchAds by toggle(default = true, icon = lucide("search-slash"))
    val skipFeedUnitRequests by toggle(default = true, icon = lucide("cloud-off"))
    val disableVideoChaining by toggle(default = false, icon = lucide("list-video"))
    val hideGames by toggle(default = false, icon = lucide("gamepad-2"))
    val hideAchievementToasts by toggle(default = true, icon = lucide("trophy"))
    val hideChatDiscovery by toggle(default = false, icon = lucide("messages-square"))
}
