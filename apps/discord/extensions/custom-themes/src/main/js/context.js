// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

exports.colorTokensModule = function (original, global, require, importDefault, importAll, module, moduleExports, dependencyMap) {
    const result = original(global, require, importDefault, importAll, module, moduleExports, dependencyMap);
    captureColorTokens(moduleExports);
    const applied = appliedTheme();
    if (applied !== null) setRawColors(applied.theme);
    syncNativeColors();
    return result;
};

exports.colorExperimentsModule = function (original, global, require, importDefault, importAll, module, moduleExports, dependencyMap) {
    const result = original(global, require, importDefault, importAll, module, moduleExports, dependencyMap);
    captureColorExperiments(moduleExports);
    return result;
};

// Every theme context inherits the root's experiments, and every style cache keys on them.
exports.RootThemeContextProvider = function (react, original, props) {
    const key = react.useSyncExternalStore(subscribe, themeExperimentsKey);
    const discordExperiments = props.enabledExperiments;
    const enabledExperiments = react.useMemo(
        () => [...(key === "" ? [] : key.split(",")), ...(discordExperiments ?? [])],
        [key, discordExperiments],
    );
    return original({ ...props, enabledExperiments });
};

exports.getSemanticColorContextFromThemeContext = function (original, themeContext) {
    const context = original(themeContext);
    const themes = themeVariants();
    if (themes.length === 0 || context.enabledExperiments?.[0] === themes[0]) return context;
    return { ...context, enabledExperiments: [...themes, ...(context.enabledExperiments ?? [])] };
};

// Native style properties cache by their arguments, in a context of their own.
exports.createNativeStyleProperties = function (original, styles) {
    const resolve = original(styles);
    return (...args) => resolve(...args, themeExperimentsKey());
};

exports.resolveTheme = function (original, systemTheme, preferences) {
    return shownTheme()?.theme.base ?? original(systemTheme, preferences);
};

exports.useColorThemeBackground = function (react, original) {
    const background = original();
    const entry = react.useSyncExternalStore(subscribe, shownTheme);
    const themeBackground = react.useSyncExternalStore(subscribe, shownBackground);
    return entry === null ? background : themeBackground;
};

exports.getCustomBackgroundGradient = function (original) {
    return shownTheme() === null ? original() : shownBackground();
};

exports.gradientPreset = function (original) {
    return shownTheme() === null ? original() : undefined;
};

exports.useActiveThemeType = function (react, original) {
    const type = original();
    const entry = react.useSyncExternalStore(subscribe, shownTheme);
    const background = react.useSyncExternalStore(subscribe, shownBackground);
    if (entry === null) return type;
    return background === null ? "default" : "custom";
};

exports.useCustomThemeDisplaySettings = function (react, original, saved) {
    const settings = original(saved);
    const entry = react.useSyncExternalStore(subscribe, shownTheme);
    const background = react.useSyncExternalStore(subscribe, shownBackground);
    if (entry === null || saved != null) return settings;
    return background === null ? undefined : { baseTheme: entry.theme.base, customTheme: background.customThemeSettings };
};

exports.useRoutedActiveGuildTheme = function (react, original) {
    const guildTheme = original();
    const entry = react.useSyncExternalStore(subscribe, shownTheme);
    return entry === null ? guildTheme : null;
};
