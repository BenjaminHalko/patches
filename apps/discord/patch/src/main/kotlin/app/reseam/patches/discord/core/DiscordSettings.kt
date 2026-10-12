// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.core

import app.reseam.patch.settings.toggle
import app.reseam.patches.bundle.lucide

object DiscordSettings {
    val betterStreamQuality by toggle("Better stream quality", summary = "Stream your screen at the resolutions and frame rates Nitro unlocks.", default = true, icon = lucide("monitor-up"))
    val betterVideoQuality by toggle("Better video quality", summary = "Use a higher video quality when preparing uploads. Upload limits stay the same.", default = true, icon = lucide("video"))
    val blockAnalytics by toggle("Block analytics", summary = "Stop Discord from logging usage events.", default = true, icon = lucide("eye-off"))
    val blockCrashReports by toggle("Block crash reports", summary = "Stop Discord from initializing its native crash reporter.", default = true, icon = lucide("bug-off"))
    val chatAnimationSwitch by toggle("Chat animation switch", summary = "Use the GIF autoplay switch to also turn off chat emoji animation.", default = true, icon = lucide("sparkles"))
    val cleanOutgoingLinks by toggle("Clean outgoing links", summary = "Remove tracking parameters from supported links when you send them.", default = true, icon = lucide("link"))
    val coloredNames by toggle("Colored names", summary = "Color members' names with their role color in mentions, the typing indicator, voice channels and member list headers.", default = true, icon = lucide("at-sign"))
    val developerMenu by toggle("Developer menu", summary = "Show Discord's existing developer tools in Settings.", default = true, icon = lucide("square-terminal"))
    val disableMessageSwipes by toggle("Disable message swipes", summary = "Stop swipes from starting replies or edits.", default = true, icon = lucide("move-horizontal"))
    val draftPreview by toggle("Draft preview", summary = "Long press the send button to see your message in the chat before sending it.", default = true, icon = lucide("message-square"))
    val freemoji by toggle("Freemoji", summary = "Send locked custom emoji as image links.", default = true, icon = lucide("smile"))
    val hideActivityInvites by toggle("Hide activity invites", summary = "Hide activity invite cards in messages.", default = true, icon = lucide("gamepad-2"))
    val hideContactSyncPrompt by toggle("Hide contact sync prompt", summary = "Remove the contact sync promotion from the friends screen.", default = true, icon = lucide("contact"))
    val hideGiftButton by toggle("Hide gift button", summary = "Remove the gift button from the message box.", default = true, icon = lucide("gift"))
    val hideGiftCards by toggle("Hide gift cards", summary = "Hide gift redemption cards in messages.", default = true, icon = lucide("ticket"))
    val hideGroupChatPromotion by toggle("Hide group chat promotion", summary = "Remove the Nitro recipient limit promotion from group chats.", default = true, icon = lucide("users"))
    val hideLinkCards by toggle("Hide link cards", summary = "Hide message preview cards while keeping text links, attachments and emoji or sticker images.", default = true, icon = lucide("link-2-off"))
    val hideNitroButtons by toggle("Hide Nitro buttons", summary = "Remove small Nitro promotion buttons from feature screens.", default = true, icon = lucide("zap-off"))
    val hidePickerPromotions by toggle("Hide picker promotions", summary = "Remove Nitro promotions from emoji and sticker search.", default = true, icon = lucide("search-x"))
    val hideQuests by toggle("Hide quests", summary = "Remove quest cards, profile buttons and the Quests settings entry.", default = true, icon = lucide("trophy"))
    val hideReactions by toggle("Hide reactions", summary = "Hide reaction chips below messages.", default = true, icon = lucide("smile-plus"))
    val hideReplyPreviews by toggle("Hide reply previews", summary = "Hide the quoted message above replies.", default = true, icon = lucide("reply"))
    val hideShopEntry by toggle("Hide shop entry", summary = "Remove Shop buttons from profiles and the Shop entry in settings.", default = true, icon = lucide("shopping-bag"))
    val hideThemeActivityPreviews by toggle("Hide theme activity previews", summary = "Remove activity cards from the Appearance preview.", default = true, icon = lucide("palette"))
    val hideTypingIndicator by toggle("Hide typing indicator", summary = "Stop others from seeing when you are typing.", default = true, icon = lucide("keyboard-off"))
    val instantChatJumps by toggle("Instant chat jumps", summary = "Jump to messages without animated scrolling.", default = true, icon = lucide("chevrons-down"))
    val jumpToFirstMessage by toggle("Jump to first message", summary = "Add a button above Jump to present that jumps to the first message.", default = true, icon = lucide("chevrons-up"))
    val keepDeletedMessages by toggle("Keep deleted messages", summary = "Keep loaded messages after deletion and mark them as deleted.", default = true, icon = lucide("message-square-x"))
    val localEditHistory by toggle("Local edit history", summary = "Show up to three previously displayed text versions of edited messages.", default = true, icon = lucide("history"))
    val localReadState by toggle("Local read state", summary = "Mark messages as read on this phone without updating your other devices.", default = true, icon = lucide("check-check"))
    val localThemes by toggle("Local themes", summary = "Use bundled Nitro themes and save your choice on this phone.", default = true, icon = lucide("paintbrush"))
    val moderatorBadges by toggle("Moderator badges", summary = "Tag the server owner, admins, managers and moderators next to their names in chat.", default = true, icon = lucide("shield"))
    val preciseTimestamps by toggle("Precise timestamps", summary = "Show seconds in chat timestamps.", default = true, icon = lucide("clock"))
    val premiumAppIcons by toggle("Premium app icons", summary = "Use the premium launcher icons included in Discord.", default = true, icon = lucide("shapes"))
    val quietSuperReactions by toggle("Quiet Super Reactions", summary = "Remove looping Super Reaction decorations.", default = true, icon = lucide("party-popper"))
    val repliesWithoutPings by toggle("Replies without pings", summary = "Start replies with mentions turned off. You can still turn them on.", default = true, icon = lucide("bell-off"))
    val revealSpoilers by toggle("Reveal spoilers", summary = "Show spoiler text and media without tapping to reveal them.", default = true, icon = lucide("eye"))
    val showBlockedMessages by toggle("Show blocked messages", summary = "Expand received messages from blocked users without unblocking them.", default = true, icon = lucide("user-x"))
    val skipMaskedLinkWarnings by toggle("Skip masked link warnings", summary = "Open masked links without the destination warning.", default = true, icon = lucide("external-link"))
    val staticChatEmoji by toggle("Static chat emoji", summary = "Stop custom emoji from animating in chat.", default = true, icon = lucide("circle-pause"))
    val staticChatGifs by toggle("Static chat GIFs", summary = "Stop GIFs from playing automatically in chat.", default = true, icon = lucide("film"))
    val staticStickers by toggle("Static stickers", summary = "Use still sticker previews and chat images.", default = true, icon = lucide("sticky-note"))
    val stickerLinks by toggle("Sticker links", summary = "Send locked PNG, APNG and GIF stickers as image links.", default = true, icon = lucide("sticker"))
}
