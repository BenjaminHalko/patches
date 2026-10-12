// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// Permission bits as Discord's API defines them.
const KICK_MEMBERS = 1n << 1n;
const BAN_MEMBERS = 1n << 2n;
const ADMINISTRATOR = 1n << 3n;
const MANAGE_CHANNELS = 1n << 4n;
const MANAGE_GUILD = 1n << 5n;
const MANAGE_MESSAGES = 1n << 13n;
const MANAGE_ROLES = 1n << 28n;
const MODERATE_MEMBERS = 1n << 40n;

const holdsAny = bits => ({ permissions }) => (permissions & bits) !== 0n;

// Ordered by rank; an author shows the first tag they qualify for.
const TAGS = [
    { text: "OWNER", color: 0xf0b232, qualifies: ({ guild, user }) => guild.ownerId === user.id },
    { text: "ADMIN", color: 0xe5484d, qualifies: holdsAny(ADMINISTRATOR) },
    { text: "MANAGER", color: 0x2fa36b, qualifies: holdsAny(MANAGE_GUILD | MANAGE_ROLES | MANAGE_CHANNELS) },
    { text: "MOD", color: 0x5865f2, qualifies: holdsAny(MANAGE_MESSAGES | KICK_MEMBERS | BAN_MEMBERS | MODERATE_MEMBERS) },
];

// Android reads colors as signed 32-bit ARGB.
const opaque = rgb => (0xff000000 | rgb) | 0;

function staffTag(guildStore, permissionHelpers, channel, user) {
    const guild = channel?.guild_id == null ? null : guildStore.default.getGuild(channel.guild_id);
    if (guild == null) return null;
    const permissions = permissionHelpers.computePermissions({ user, context: guild, checkElevated: false });
    return TAGS.find(tag => tag.qualifies({ guild, user, permissions })) ?? null;
}

exports.getTagProperties = function (guildStore, permissionHelpers, original, properties) {
    const tag = original(properties);
    const author = properties.message.author;
    if (tag.tagText != null || author.bot) return tag;
    const staff = staffTag(guildStore, permissionHelpers, properties.channel, author);
    if (staff == null) return tag;
    return {
        ...tag,
        tagText: staff.text,
        tagAccessibilityLabel: staff.text,
        tagTextColor: opaque(0xffffff),
        tagBackgroundColor: opaque(staff.color),
    };
};
