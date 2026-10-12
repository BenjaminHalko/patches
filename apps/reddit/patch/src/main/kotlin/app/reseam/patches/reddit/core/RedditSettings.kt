// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.reddit.core

import app.reseam.patch.settings.toggle
import app.reseam.patches.bundle.lucide

object RedditSettings {
    val hideFeedAds by toggle("Hide feed ads", summary = "Removes promoted posts from feeds and search results.", default = true, icon = lucide("megaphone-off"))
    val hideCommentAds by toggle("Hide comment ads", summary = "Removes ads under posts and between comments.", default = true, icon = lucide("message-square-off"))
    val hideTrendingUnits by toggle("Hide Trending units", summary = "Removes Trending Today units from feeds.", default = true, icon = lucide("trending-up"))
    val hidePostCarousels by toggle("Hide post carousels", summary = "Removes \"more from\" and news carousels from feeds.", default = true, icon = lucide("gallery-horizontal"))
    val hidePromoUnits by toggle("Hide promos and onboarding", summary = "Removes Reddit promos and onboarding units from feeds.", default = true, icon = lucide("tag"))
    val hideCommunityUnits by toggle("Hide surveys, AMAs and highlights", summary = "Removes surveys and AMA carousels. Also hides pinned posts in communities.", default = false, icon = lucide("clipboard-list"))
    val hideContributionNudges by toggle("Hide contribution nudges", summary = "Removes end-of-feed prompts and nudges to post or comment.", default = true, icon = lucide("pencil-off"))
    val hideAnswerCards by toggle("Hide Answers and Copilot cards", summary = "Removes Answers suggestions and Copilot cards from posts and feeds.", default = true, icon = lucide("bot-off"))
    val hideAwardBar by toggle("Hide award bar", summary = "Removes the awards row under feed posts.", default = false, icon = lucide("award"))
    val hideRelatedPosts by toggle("Hide related posts", summary = "Removes related posts under comments.", default = true, icon = lucide("list-x"))
    val hideRecommendedPosts by toggle("Hide recommended posts", summary = "Removes suggested posts from communities you don't follow in Home.", default = false, icon = lucide("thumbs-down"))
    val hideRecommendedCommunities by toggle("Hide recommended communities", summary = "Removes the community suggestions shelf from feeds.", default = true, icon = lucide("users"))
    val hideTrendingSearches by toggle("Hide trending searches", summary = "Removes the Trending section from the search screen.", default = true, icon = lucide("search-x"))
    val ignoreSuggestedSort by toggle("Always use my default comment sort", summary = "Ignores the sort a post or community suggests.", default = false, icon = lucide("arrow-down-wide-narrow"))
    val hideCreateTab by toggle("Hide Create tab", summary = "Removes Create from the bottom bar.", default = true, icon = lucide("square-plus"))
    val hideInboxTab by toggle("Hide Inbox tab", summary = "Removes the only Inbox entry point.", default = false, icon = lucide("inbox"))
    val hidePremiumSettings by toggle("Hide Premium and Pro settings", summary = "Removes Premium and Reddit Pro rows from settings.", default = true, icon = lucide("settings"))
    val blockForcedUpdates by toggle("Block remote forced updates", summary = "Reddit cannot remotely block this version.", default = true, icon = lucide("shield-check"))
    val disableScreenshotBanner by toggle("Disable screenshot banner", summary = "No share prompt or \"Reddit detected this screenshot\" notice.", default = true, icon = lucide("camera"))
    val skipNsfwWarning by toggle("Skip NSFW community warning", summary = "Opens mature communities without the confirmation dialog.", default = false, icon = lucide("triangle-alert"))
    val blockAnalytics by toggle("Block Reddit analytics", summary = "Stops Reddit's event and metric logging.", default = true, icon = lucide("eye-off"))
    val blockAppsFlyer by toggle("Block AppsFlyer tracking", summary = "Turns off the AppsFlyer install and attribution tracker.", default = true, icon = lucide("radar"))
    val disableAppRatePrompt by toggle("Disable rate-the-app prompt", summary = "No prompt asking you to rate the app.", default = true, icon = lucide("star-off"))
    val hideAnswers by toggle("Hide Reddit Answers", summary = "Removes the Answers tab and AI answer entry points.", default = true, icon = lucide("message-circle-question"))
    val hideNotificationPrompts by toggle("Hide notification prompts", summary = "No banners or sheets asking to turn on notifications.", default = true, icon = lucide("bell-off"))
    val hideUpdateNudges by toggle("Hide app update nudges", summary = "No banners asking you to update the app.", default = true, icon = lucide("refresh-cw-off"))
    val hideShareNudges by toggle("Hide share and repost nudges", summary = "No prompts asking you to share or repost.", default = true, icon = lucide("share-2"))
    val hideComposerPrompts by toggle("Hide composer inspiration prompts", summary = "Removes inspiration prompts from the post composer.", default = true, icon = lucide("lightbulb-off"))
    val hidePremiumUpsells by toggle("Hide Premium upsells", summary = "Removes Premium offers and upgrade prompts.", default = true, icon = lucide("gem"))
    val disableAdAutoplay by toggle("Disable ad video autoplay", summary = "Stops video ads from playing automatically.", default = true, icon = lucide("circle-pause"))
    val hideSearchAds by toggle("Hide search ads", summary = "Removes promoted results from search.", default = true, icon = lucide("search-slash"))
    val skipFeedUnitRequests by toggle("Skip recommendation unit requests", summary = "Home and Popular stop requesting Trending, carousel and onboarding units.", default = true, icon = lucide("cloud-off"))
    val disableVideoChaining by toggle("Disable full-screen video chaining", summary = "Turns off vertical chaining to more videos in the full-screen player.", default = false, icon = lucide("list-video"))
    val hideGames by toggle("Hide Games", summary = "Removes the Games tab and feed.", default = false, icon = lucide("gamepad-2"))
    val hideAchievementToasts by toggle("Hide achievement toasts", summary = "No streak and progress pop-ups.", default = true, icon = lucide("trophy"))
    val hideChatDiscovery by toggle("Hide chat channel discovery", summary = "Removes chat channel suggestions.", default = false, icon = lucide("messages-square"))
}
