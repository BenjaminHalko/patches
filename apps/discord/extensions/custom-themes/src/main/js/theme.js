// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// A semantic color wins over the raw color its token uses, a raw color over the palette for its family.

const THEME_FORMAT = "reseam.discord-theme";
const THEME_VERSION = 1;
const THEME_FILE_SUFFIX = ".reseam-theme.json";

const THEME_BASES = ["darker", "midnight", "light"];

const PALETTE_FAMILIES = {
    neutral: ["NEUTRAL", "PRIMARY", "PLUM", "OPACITY"],
    brand: ["BLURPLE", "BRAND", "OPACITY_BLURPLE"],
    red: ["RED_NEW", "RED", "OPACITY_RED"],
    green: ["GREEN_NEW", "GREEN", "OPACITY_GREEN"],
    yellow: ["YELLOW_NEW", "YELLOW", "OPACITY_YELLOW"],
    blue: ["BLUE_NEW", "BLUE", "OPACITY_BLUE"],
};

const COLOR_NAME = /^[A-Z][A-Z0-9_]*$/;

const MAX_BACKGROUND_COLORS = 5;
const DEFAULT_BACKGROUND_MIX = 74;

function requireText(value, field) {
    if (typeof value !== "string") throw new Error(`The theme's ${field} is not text`);
    return value;
}

function parseColors(value, field) {
    if (value === undefined) return {};
    if (value === null || typeof value !== "object") throw new Error(`The theme's ${field} colors are not an object`);
    return Object.fromEntries(Object.entries(value).map(([name, color]) => {
        if (!COLOR_NAME.test(name)) throw new Error(`${name} is not a Discord color name`);
        return [name, parseColor(requireText(color, `${field} color ${name}`))];
    }));
}

function parsePalette(key, value) {
    if (!(key in PALETTE_FAMILIES)) throw new Error(`${key} is not a palette`);
    if (value === null || typeof value !== "object") throw new Error(`The ${key} palette is not an object`);
    const stops = Object.entries(value).map(([tone, color]) => {
        const number = Number(tone);
        if (!Number.isFinite(number) || number < 0 || number > 100) throw new Error(`${tone} is not a tone from 0 to 100`);
        return [number, parseColor(requireText(color, `${key} palette color`))];
    }).sort(([a], [b]) => a - b);
    if (stops.length === 0) throw new Error(`The ${key} palette has no colors`);
    return stops;
}

function parseBackground(value) {
    if (value === undefined || value === null) return null;
    if (typeof value !== "object" || !Array.isArray(value.colors)) throw new Error("The theme's background has no colors");
    if (value.colors.length === 0 || value.colors.length > MAX_BACKGROUND_COLORS) {
        throw new Error(`A background has 1 to ${MAX_BACKGROUND_COLORS} colors`);
    }
    const angle = value.angle ?? 0;
    const mix = value.mix ?? DEFAULT_BACKGROUND_MIX;
    if (typeof angle !== "number" || angle < 0 || angle > 360) throw new Error(`${angle} is not an angle from 0 to 360`);
    if (typeof mix !== "number" || mix < 0 || mix > 100) throw new Error(`${mix} is not a background strength from 0 to 100`);
    return { colors: value.colors.map(color => withAlpha(parseColor(requireText(color, "background color")), 1)), angle, mix };
}

function parseTheme(document) {
    if (document === null || typeof document !== "object") throw new Error("A theme file holds a JSON object");
    if (document.format !== THEME_FORMAT) throw new Error("This is not a Reseam Discord theme");
    if (document.version !== THEME_VERSION) throw new Error(`Theme version ${document.version} is not supported`);
    const name = requireText(document.name, "name").trim();
    if (name === "") throw new Error("The theme has no name");
    if (!THEME_BASES.includes(document.base)) throw new Error(`${document.base} is not a base theme`);
    const palettes = document.palettes ?? {};
    if (palettes === null || typeof palettes !== "object") throw new Error("The theme's palettes are not an object");
    return {
        name,
        author: document.author === undefined ? null : requireText(document.author, "author"),
        base: document.base,
        palettes: Object.fromEntries(Object.entries(palettes).map(([key, stops]) => [key, parsePalette(key, stops)])),
        raw: parseColors(document.raw, "raw"),
        semantic: parseColors(document.semantic, "semantic"),
        background: parseBackground(document.background),
    };
}

function serializeColors(colors) {
    return Object.fromEntries(Object.entries(colors).map(([name, color]) => [name, formatColor(color)]));
}

function serializeTheme(theme) {
    return {
        format: THEME_FORMAT,
        version: THEME_VERSION,
        name: theme.name,
        ...(theme.author === null ? {} : { author: theme.author }),
        base: theme.base,
        palettes: Object.fromEntries(Object.entries(theme.palettes).map(([key, stops]) =>
            [key, Object.fromEntries(stops.map(([tone, color]) => [String(tone), formatColor(color)]))])),
        raw: serializeColors(theme.raw),
        semantic: serializeColors(theme.semantic),
        ...(theme.background === null ? {} : {
            background: { colors: theme.background.colors.map(opaqueHex), angle: theme.background.angle, mix: theme.background.mix },
        }),
    };
}

function paletteKeyOf(rawName) {
    const family = rawName.replace(/_\d+$/, "");
    return Object.keys(PALETTE_FAMILIES).find(key => PALETTE_FAMILIES[key].includes(family)) ?? null;
}

function themeFileName(theme) {
    const name = theme.name.replace(/[\\/:*?"<>|.]+/g, "").trim().replace(/\s+/g, "-") || "theme";
    return `${name}${THEME_FILE_SUFFIX}`;
}
