// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.core

import app.reseam.patch.MetroModule
import app.reseam.patch.function

/** GuildStore; its default export is the store. Named by the store's guild count. */
internal val guildStore: MetroModule = function {
    name("getGuildCount")
    strings("database")
    paramCount(0)
}.module

/** Discord's permission helpers, such as `computePermissions({ user, context })`. */
internal val permissionHelpers: MetroModule = function {
    name("computePermissions")
    strings("excludeGuildPermissions", "isViewingRoles")
    paramCount(1)
}.module