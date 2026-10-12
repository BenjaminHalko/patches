// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.x.core

import app.reseam.patch.settings.Choice
import app.reseam.patch.settings.choice
import app.reseam.patch.settings.toggle
import app.reseam.patches.bundle.lucide

object XSettings {
    val defaultToFollowing by toggle("Default to Following", summary = "Opens Following on launch and stops automatic switches to For You.", default = true, icon = lucide("users"))
    val hidePromotionPrompts by toggle("Hide promotion prompts", summary = "Removes Boost and Promote prompts on your posts.", default = true, icon = lucide("rocket"))
    val hideAds by toggle("Hide ads", summary = "Removes promoted posts and Google ads from timelines.", default = true, icon = lucide("megaphone-off"))
    val hidePremiumUpsells by toggle("Hide Premium upsells", summary = "Removes Upgrade buttons, Get Verified prompts and Premium offers.", default = true, icon = lucide("gem"))
    val hideAppOpenPaywall by toggle("Hide app-open paywall", summary = "Stops the full-screen Premium offer X can show at launch.", default = true, icon = lucide("lock"))
    val unlockPremium by toggle("Unlock Premium", summary = "Passes X's on-device Premium checks. Some features still need a subscription.", default = true, icon = lucide("badge-check"))
    val downloadAnyVideo by toggle("Download any video", summary = "Offers Download Video even when the author turned downloads off.", default = true, icon = lucide("download"))
    val downloadWithoutWatermark by toggle("Download without watermark", summary = "Saves the original file instead of the watermarked copy.", default = true, icon = lucide("droplet-off"))

    val hideGrokTab by toggle("Hide Grok tab", summary = "Removes Grok from the bottom bar.", default = true, icon = lucide("bot-off"))
    val hideGrokPostButton by toggle("Hide Grok on posts and profiles", summary = "Removes the Explain-this-post and Ask Grok buttons.", default = true, icon = lucide("sparkles"))
    val hideGrokDrawerEntry by toggle("Hide Grok in drawer", summary = "Removes Get Grok and the Grok bot entry.", default = true, icon = lucide("panel-left"))
    val hideGrokPrompts by toggle("Hide Grok and Imagine prompts", summary = "Removes the Download Grok and Imagine prompts on posts and profiles.", default = true, icon = lucide("message-square-dashed"))
    val hideGrokImageTools by toggle("Hide Make video and Edit image", summary = "Removes the Grok Imagine items from the post menu and the composer.", default = true, icon = lucide("image-off"))
    val hideGrokComposer by toggle("Hide Grok in composer", summary = "Removes the Grok button from the composer.", default = true, icon = lucide("pen-off"))
    val disableGrokTranslations by toggle("Disable Grok translations", summary = "No automatic Grok translation of posts, bios and Community Notes.", default = false, icon = lucide("languages"))
    val hideLiveBar by toggle("Hide Live bar", summary = "Removes the row of live Spaces and broadcasts above Home.", default = true, icon = lucide("radio"))
    val hideLiveAvatarRings by toggle("Hide live avatar rings", summary = "No live rings around the avatars of people who are live.", default = false, icon = lucide("circle-dot"))
    val hideRecommendations by toggle("Hide recommendations", summary = "Removes Who to follow, Subscribe to, and Communities to join modules.", default = false, icon = lucide("user-plus"))
    val hideViewCounts by toggle("Hide view counts", summary = "Removes Views from the post action bar and the post detail line.", default = false, icon = lucide("chart-no-axes-column"))
    val hideNotificationPrompts by toggle("Hide notification prompts", summary = "Stops the prompts and banner asking to turn on notifications.", default = true, icon = lucide("bell-off"))
    val blockInstalledAppsScan by toggle("Block installed-apps scan", summary = "Stops X from reporting your installed apps for ad targeting.", default = true, icon = lucide("scan-search"))
    val disableAdTracking by toggle("Disable ad tracking", summary = "Turns off the advertising-id and automatic analytics reporting switches.", default = true, icon = lucide("eye-off"))

    val shareLinkProvider by choice("Share link provider", summary = "Host used for shared post links.", default = "fixvx.com",
        choices = listOf(Choice("fixvx.com", "FixVX"), Choice("fxtwitter.com", "FxTwitter"), Choice("vxtwitter.com", "vxTwitter"),
            Choice("fixupx.com", "FixupX"), Choice("off", "Off (x.com)")), icon = lucide("share-2"))
}
