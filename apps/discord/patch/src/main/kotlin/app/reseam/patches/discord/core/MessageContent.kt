// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.core

import app.reseam.patch.function

/** Builds one chat row from a message; patches that change how messages look wrap it. */
internal val createMessageContent = function {
    name("createMessageContent")
    strings("renderGiftCode", "animatingStickerMessageId", "createActivityInstanceEmbed")
    paramCount(1)
}

/** Reads a channel's loaded messages, the records chat rows are built from. */
internal val getOrCreate = function {
    name("getOrCreate")
    strings("_channelMessages", "JumpType")
    paramCount(1)
}

/** Stores a channel's changed messages; the chat redraws the rows whose message changed. */
internal val commit = function {
    name("commit")
    strings("_channelMessages", "channelId")
    paramCount(1)
}
