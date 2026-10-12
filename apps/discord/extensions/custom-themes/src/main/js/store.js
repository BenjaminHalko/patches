// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

// Kept in Discord's private storage, so Discord's synced appearance settings never change.

const THEMES_MODULE = "ReseamDiscordThemes";
const LIBRARY_VERSION = 1;

let library = null;
let builtInThemes = null;
let preview = null;
let nativeVariant = "";
let themeActions = null;
let snapshot = 0;
let variants = null;
let background = { entry: null, style: "gradient", value: null };
const listeners = new Set();

function withoutKey(object, key) {
    return Object.fromEntries(Object.entries(object).filter(([other]) => other !== key));
}

function themesModule() {
    return globalThis.nativeModuleProxy[THEMES_MODULE];
}

function parseLibrary(document) {
    return {
        themes: document.themes.map(({ id, revision, theme }) => ({ id, revision, builtIn: false, theme: parseTheme(theme) })),
        applied: document.applied,
        backgroundStyles: document.backgroundStyles,
    };
}

function loadLibrary() {
    if (library !== null) return library;
    const saved = themesModule().readThemes();
    library = saved === "" ? { themes: [], applied: null, backgroundStyles: {} } : parseLibrary(JSON.parse(saved));
    return library;
}

function saveLibrary() {
    themesModule().writeThemes(JSON.stringify({
        version: LIBRARY_VERSION,
        themes: library.themes.map(entry => ({ id: entry.id, revision: entry.revision, theme: serializeTheme(entry.theme) })),
        applied: library.applied,
        backgroundStyles: library.backgroundStyles,
    }));
}

function builtIns() {
    builtInThemes ??= [{ id: RESEAM_THEME_ID, revision: 1, builtIn: true, theme: parseTheme(RESEAM_THEME_DOCUMENT) }];
    return builtInThemes;
}

function allThemes() {
    return [...builtIns(), ...loadLibrary().themes];
}

function findTheme(id) {
    return allThemes().find(entry => entry.id === id) ?? null;
}

function subscribe(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
}

function librarySnapshot() {
    return snapshot;
}

function notify() {
    snapshot++;
    variants = null;
    listeners.forEach(listener => listener());
}

function setThemeActions(actions) {
    themeActions = actions;
}

function refreshAppTheme() {
    themeActions.refreshTheme();
}

function appliedTheme() {
    const id = loadLibrary().applied;
    return id === null ? null : findTheme(id);
}

function syncNativeColors() {
    const applied = appliedTheme();
    const variant = applied === null ? "" : variantName(applied);
    if (variant === nativeVariant) return;
    nativeVariant = variant;
    themesModule().applyNativeColors(applied === null ? "" : JSON.stringify(nativeColors(applied)));
}

// A previewed theme shows in place of the applied one, except in native views.
function shownTheme() {
    return preview ?? appliedTheme();
}

function shownThemeChanged() {
    setRawColors(shownTheme()?.theme ?? null);
    refreshAppTheme();
    notify();
}

function appliedThemeChanged() {
    syncNativeColors();
    shownThemeChanged();
}

function backgroundStyle(id) {
    return loadLibrary().backgroundStyles[id] ?? "gradient";
}

function setBackgroundStyle(id, style) {
    const lib = loadLibrary();
    const others = withoutKey(lib.backgroundStyles, id);
    lib.backgroundStyles = style === "gradient" ? others : { ...others, [id]: style };
    saveLibrary();
    notify();
}

function shownBackground() {
    const shown = shownTheme();
    const style = shown === null ? "gradient" : backgroundStyle(shown.id);
    if (background.entry !== shown || background.style !== style) {
        background = { entry: shown, style, value: shown === null || style === "none" ? null : themeBackground(shown, style === "gradient") };
    }
    return background.value;
}

function applyTheme(id) {
    const lib = loadLibrary();
    if (lib.applied === id) return;
    lib.applied = id;
    saveLibrary();
    appliedThemeChanged();
}

function newThemeId() {
    return `${Date.now().toString(36)}${Math.random().toString(36).slice(2, 8)}`;
}

function addTheme(theme) {
    const entry = { id: newThemeId(), revision: 1, builtIn: false, theme };
    loadLibrary().themes.push(entry);
    saveLibrary();
    notify();
    return entry;
}

function findSameTheme(theme) {
    const text = JSON.stringify(serializeTheme(theme));
    return allThemes().find(entry => JSON.stringify(serializeTheme(entry.theme)) === text) ?? null;
}

function updateTheme(id, theme) {
    const lib = loadLibrary();
    const index = lib.themes.findIndex(entry => entry.id === id);
    const previous = lib.themes[index];
    const entry = { ...previous, revision: previous.revision + 1, theme };
    lib.themes = lib.themes.map((other, at) => at === index ? entry : other);
    saveLibrary();
    if (lib.applied === id) appliedThemeChanged(); else notify();
    return entry;
}

function deleteTheme(id) {
    const lib = loadLibrary();
    lib.themes = lib.themes.filter(entry => entry.id !== id);
    lib.backgroundStyles = withoutKey(lib.backgroundStyles, id);
    const wasApplied = lib.applied === id;
    if (wasApplied) lib.applied = null;
    saveLibrary();
    if (wasApplied) appliedThemeChanged(); else notify();
}

function setPreview(entry) {
    if (preview === entry) return;
    preview = entry;
    shownThemeChanged();
}

function themeVariants() {
    if (variants === null) variants = [shownTheme(), appliedTheme()].filter(entry => entry !== null).map(themeVariant);
    return variants;
}

function themeExperimentsKey() {
    return themeVariants().join(",");
}
