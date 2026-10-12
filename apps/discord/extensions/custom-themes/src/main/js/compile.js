// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// A theme compiles to a semantic color variant, the kind Discord's resolver picks by the theme
// context's enabledExperiments: experiments[token][variant][theme] = { raw, opacity }.
const tables = { tokens: null, raw: null, pristineRaw: null, semantic: null, experiments: null };
const compiledVariants = new Map();
const CUSTOM_GRADIENT_ENTRY = "customBackgroundGradient";
const swatches = new WeakMap();

function captureColorTokens(moduleExports) {
    tables.tokens = moduleExports.default;
    tables.raw = moduleExports.RawColor;
    tables.pristineRaw = { ...moduleExports.RawColor };
    tables.semantic = moduleExports.SemanticColor;
}

function captureColorExperiments(moduleExports) {
    tables.experiments = moduleExports._private.SemanticColorExperiments;
}

function rawColorOf(theme, name) {
    if (name in theme.raw) return theme.raw[name];
    const color = parseColor(tables.pristineRaw[name]);
    const key = paletteKeyOf(name);
    const palette = key === null ? undefined : theme.palettes[key];
    return palette === undefined ? color : paletteAt(palette, toneOf(color), color.a);
}

function compileTheme(theme) {
    const colors = {};
    for (const [token, definition] of Object.entries(tables.semantic)) {
        const value = definition[theme.base];
        if (value !== undefined) colors[token] = withAlpha(rawColorOf(theme, value.raw), value.opacity);
    }
    return { ...colors, ...theme.semantic };
}

function variantName(entry) {
    return `reseam-theme/${entry.id}/${entry.revision}`;
}

function themeVariant(entry) {
    const name = variantName(entry);
    if (compiledVariants.has(name)) return name;
    const colors = compileTheme(entry.theme);
    for (const [token, color] of Object.entries(colors)) {
        if (!(token in tables.semantic)) continue;
        const raw = `RESEAM_${opaqueHex(color).slice(1)}`;
        tables.raw[raw] = opaqueHex(color);
        (tables.experiments[token] ??= {})[name] = { [entry.theme.base]: { raw, opacity: color.a } };
    }
    compiledVariants.set(name, colors);
    return name;
}

function compiledColors(entry) {
    return compiledVariants.get(themeVariant(entry));
}

// Code that read a raw color while it loaded keeps it, so startup sets them first.
function setRawColors(theme) {
    for (const [name, color] of Object.entries(tables.pristineRaw)) {
        const key = paletteKeyOf(name);
        const themed = theme !== null && (name in theme.raw || (key !== null && key in theme.palettes));
        tables.raw[name] = themed ? formatColor(rawColorOf(theme, name)) : color;
    }
}

function themeSwatch(theme) {
    if (theme.background !== null) return theme.background.colors.map(opaqueHex);
    if (!swatches.has(theme)) {
        const colors = compileTheme(theme);
        swatches.set(theme, [opaqueHex(colors.BACKGROUND_BASE_LOW), opaqueHex(colors.BACKGROUND_BRAND)]);
    }
    return swatches.get(theme);
}

function nativeColors(entry) {
    const colors = compiledColors(entry);
    return { base: entry.theme.base, colors: Object.fromEntries(Object.entries(colors).map(([token, color]) => [token, argb(color)])) };
}

function themeBackground(entry, gradient) {
    const background = entry.theme.background;
    if (background === null) return null;
    const colors = gradient ? background.colors : [background.colors[0], background.colors[0]];
    return {
        type: CUSTOM_GRADIENT_ENTRY,
        theme: entry.theme.base,
        getName: () => entry.theme.name,
        customThemeSettings: {
            colors: colors.map(opaqueHex),
            gradientColorStops: [],
            gradientAngle: background.angle,
            baseMix: background.mix,
        },
    };
}

function discordPaletteColor(key) {
    return withAlpha(parseColor(tables.pristineRaw[`${PALETTE_FAMILIES[key][0]}_50`]), 1);
}
