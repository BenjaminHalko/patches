// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.guild

import app.reseam.discord.settings.DiscordStrings
import app.reseam.patch.ExtJsModule
import app.reseam.patch.function
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patches.discord.core.DISCORD
import app.reseam.discord.settings.DiscordSettings
import app.reseam.patches.discord.core.designSystem
import app.reseam.patches.discord.core.discordSettings
import app.reseam.patches.discord.core.guildChannelStore
import app.reseam.patches.discord.core.guildMemberCountStore
import app.reseam.patches.discord.core.guildMemberStore
import app.reseam.patches.discord.core.guildRoleStore
import app.reseam.patches.discord.core.react
import app.reseam.patches.discord.core.userStore

val serverDetails = patch("Server details") {
    description("Show a server's owner, creation date, members, boosts and more in its options.")
    compatibleWith(DISCORD)
    settings(discordSettings, section(DiscordStrings.sectionServers, DiscordSettings.serverDetails))

    execute {
        gate(DiscordSettings.serverDetails) {
            guildActionSheetSecondaryActions.wrap(
                ServerDetails.GuildActionSheetSecondaryActions,
                react,
                designSystem,
                userStore,
                guildMemberStore,
                guildMemberCountStore,
                guildRoleStore,
                guildChannelStore,
            )
        }
    }
}

private object ServerDetails : ExtJsModule("discord-server-details") {
    val GuildActionSheetSecondaryActions by export()
}

/** The lower group of a server's options sheet, such as notifications and privacy. */
private val guildActionSheetSecondaryActions = function {
    name("GuildActionSheetSecondaryActions")
    strings("useCanReportRaid", "useGuildIncidentsState")
    paramCount(1)
}
