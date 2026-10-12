// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

const THEMES_ROUTE = "RESEAM_THEMES";
const THEME_EDITOR_ROUTE = "RESEAM_THEME_EDITOR";
const BASE_NAMES = { darker: "Dark", midnight: "Midnight", light: "Light" };
const PALETTE_NAMES = { neutral: "Neutral", brand: "Brand", red: "Red", green: "Green", yellow: "Yellow", blue: "Blue" };
const PALETTE_SAMPLE_TONE = 50;
const BACKGROUND_ANGLES = [0, 45, 90, 135, 180, 225, 270, 315];

let themeScreens = null;

function newTheme() {
    return { name: "New theme", author: null, base: appliedTheme()?.theme.base ?? "darker", palettes: {}, raw: {}, semantic: {}, background: null };
}

function tokenLabel(token) {
    const words = token.toLowerCase().split("_");
    return [words[0][0].toUpperCase() + words[0].slice(1), ...words.slice(1)].join(" ");
}

function themeSummary(entry) {
    return [BASE_NAMES[entry.theme.base], entry.builtIn ? "Built in" : entry.theme.author].filter(Boolean).join(" · ");
}

function createThemeScreens(modules) {
    const { react, reactNative, design, colorTokens, colorPicker, icons } = modules;
    const h = react.createElement;
    const { FlatList, ScrollView, View } = reactNative;
    const { Stack, TableRow, TableRowGroup, TableRadioGroup, TableRadioRow, TextInput, SearchField } = design;
    const colors = colorTokens.default.colors;
    const space = colorTokens.default.space;

    const useStyles = design.createStyles({
        screen: { padding: space.PX_16, paddingBottom: space.PX_24 },
        swatch: { width: 32, height: 32, borderRadius: 16, overflow: "hidden", flexDirection: "row", borderWidth: 1, borderColor: colors.BORDER_SUBTLE },
        swatchHalf: { flex: 1 },
        colorSample: { width: 28, height: 28, borderRadius: 8, borderWidth: 1, borderColor: colors.BORDER_SUBTLE },
        search: { marginBottom: space.PX_8 },
        trailing: { flexDirection: "row", alignItems: "center", gap: space.PX_12 },
    });

    function Swatch({ theme }) {
        const styles = useStyles();
        return h(View, { style: styles.swatch },
            ...themeSwatch(theme).map((color, index) => h(View, { key: index, style: [styles.swatchHalf, { backgroundColor: color }] })));
    }

    function ColorSample({ color }) {
        const styles = useStyles();
        return h(View, { style: [styles.colorSample, { backgroundColor: formatColor(color) }] });
    }

    // Discord's picker reports the color on Select and again as it closes.
    function pickColor(color, onPick, { adding = false } = {}) {
        let last = adding ? null : rgbInt(color);
        colorPicker.default({
            color: rgbInt(color),
            onSelect: value => {
                if (value === last) return;
                last = value;
                onPick(fromRgbInt(value, color.a));
            },
        });
    }

    function ThemeTrailing({ applied, onEdit, name }) {
        const styles = useStyles();
        return h(View, { style: styles.trailing },
            applied ? h(TableRow.Icon, { IconComponent: icons.checkmark }) : null,
            onEdit === null ? null : h(design.IconButton, {
                icon: h(icons.pencil, { size: "sm" }),
                variant: "secondary",
                size: "sm",
                accessibilityLabel: `Edit ${name}`,
                onPress: onEdit,
            }));
    }

    function ThemesScreen({ navigation }) {
        const styles = useStyles();
        react.useSyncExternalStore(subscribe, librarySnapshot);
        const applied = appliedTheme();
        const open = entry => navigation.navigate(THEME_EDITOR_ROUTE, { themeId: entry.id });
        return h(ScrollView, { contentContainerStyle: styles.screen },
            h(Stack, { spacing: 24 },
                h(TableRowGroup, { title: "Themes", description: "Tap a theme to use it. Themes stay on this phone; share one to send its file." },
                    h(TableRow, {
                        label: "Discord's theme",
                        subLabel: "The theme picked in Appearance",
                        trailing: h(ThemeTrailing, { applied: applied === null, onEdit: null, name: "" }),
                        onPress: () => applyTheme(null),
                    }),
                    ...allThemes().map(entry => h(TableRow, {
                        key: entry.id,
                        label: entry.theme.name,
                        subLabel: themeSummary(entry),
                        icon: h(Swatch, { theme: entry.theme }),
                        trailing: h(ThemeTrailing, { applied: entry === applied, onEdit: () => open(entry), name: entry.theme.name }),
                        onPress: () => applyTheme(entry.id),
                    }))),
                h(TableRowGroup, null,
                    h(TableRow, {
                        label: "New theme",
                        icon: h(TableRow.Icon, { IconComponent: icons.plus }),
                        onPress: () => open(addTheme(newTheme())),
                    }))));
    }

    function NameField({ entry }) {
        const [name, setName] = react.useState(entry.theme.name);
        const save = () => {
            const trimmed = name.trim();
            if (trimmed !== "" && trimmed !== entry.theme.name) updateTheme(entry.id, { ...entry.theme, name: trimmed });
        };
        return h(TextInput, { label: "Name", value: name, onChange: setName, onBlur: save, onSubmitEditing: save, returnKeyType: "done" });
    }

    function BackgroundSection({ entry }) {
        const theme = entry.theme;
        const background = theme.background;
        const setBackground = next => updateTheme(entry.id, { ...theme, background: next });
        const withColors = colors => setBackground({ angle: background?.angle ?? 0, mix: background?.mix ?? DEFAULT_BACKGROUND_MIX, colors });
        const colors = background?.colors ?? [];
        const editColor = index => design.showSimpleActionSheet({
            key: "ReseamThemeBackgroundColor",
            header: { title: `Background color ${index + 1}` },
            options: [
                { label: "Pick a color", onPress: () => pickColor(colors[index], color => withColors(colors.map((other, at) => at === index ? color : other))) },
                ...(colors.length > 1 ? [{ label: "Remove color", isDestructive: true, onPress: () => withColors(colors.filter((_, at) => at !== index)) }] : []),
            ],
        });
        const addColor = () => pickColor(colors.at(-1) ?? compiledColors(entry).BACKGROUND_BRAND,
            color => withColors([...colors, withAlpha(color, 1)]), { adding: true });
        const presets = { 50: "Soft", [DEFAULT_BACKGROUND_MIX]: "Default", 100: "Vivid" };
        const strengths = { ...presets, ...(background === null ? {} : { [background.mix]: presets[background.mix] ?? `${background.mix}%` }) };
        const styleChoice = background === null ? null : h(TableRadioGroup, {
            title: "Show background as",
            description: "Applies on this phone; a shared file keeps the theme's background.",
            value: backgroundStyle(entry.id),
            onChange: style => setBackgroundStyle(entry.id, style),
            hasIcons: false,
        },
            h(TableRadioRow, { key: "gradient", value: "gradient", label: "Gradient" }),
            h(TableRadioRow, { key: "solid", value: "solid", label: "Solid", subLabel: "The first color only" }),
            h(TableRadioRow, { key: "none", value: "none", label: "None", subLabel: "The theme's colors without a background" }));
        if (entry.builtIn) return styleChoice;
        return h(Stack, { spacing: 24 },
            styleChoice,
            h(TableRowGroup, { title: "Background", description: "Drawn behind the app like a Nitro theme, with see-through surfaces over it." },
                ...colors.map((color, index) => h(TableRow, {
                    key: index,
                    label: `Color ${index + 1}`,
                    subLabel: opaqueHex(color),
                    trailing: h(ColorSample, { color }),
                    arrow: true,
                    onPress: () => editColor(index),
                })),
                colors.length < MAX_BACKGROUND_COLORS
                    ? h(TableRow, { label: "Add color", icon: h(TableRow.Icon, { IconComponent: icons.plus }), onPress: addColor })
                    : null,
                background === null ? null : h(TableRow, { label: "Remove background", variant: "danger", onPress: () => setBackground(null) })),
            background === null ? null : h(TableRadioGroup, { title: "Direction", value: background.angle, onChange: angle => setBackground({ ...background, angle }), hasIcons: false },
                ...BACKGROUND_ANGLES.map(angle => h(TableRadioRow, { key: angle, value: angle, label: `${angle}°` }))),
            background === null ? null : h(TableRadioGroup, { title: "Strength", value: background.mix, onChange: mix => setBackground({ ...background, mix }), hasIcons: false },
                ...Object.entries(strengths).map(([mix, label]) => h(TableRadioRow, { key: mix, value: Number(mix), label }))));
    }

    function EditorHeader({ entry, navigation, search, setSearch }) {
        const styles = useStyles();
        const theme = entry.theme;
        const update = changes => updateTheme(entry.id, { ...theme, ...changes });
        const choosePalette = key => {
            const stops = theme.palettes[key];
            const pick = () => pickColor(stops === undefined ? discordPaletteColor(key) : paletteAt(stops, PALETTE_SAMPLE_TONE, 1),
                seed => update({ palettes: { ...theme.palettes, [key]: paletteFromSeed(seed) } }));
            if (stops === undefined) return pick();
            design.showSimpleActionSheet({
                key: "ReseamThemePalette",
                header: { title: `${PALETTE_NAMES[key]} palette` },
                options: [
                    { label: "Pick a color", onPress: pick },
                    { label: "Use Discord's colors", onPress: () => update({ palettes: withoutKey(theme.palettes, key) }) },
                ],
            });
        };
        return h(Stack, { spacing: 24, style: styles.search },
            entry.builtIn ? null : h(NameField, { entry }),
            entry.builtIn ? null : h(TableRadioGroup, { title: "Base theme", value: theme.base, onChange: base => update({ base }), hasIcons: false },
                ...THEME_BASES.map(base => h(TableRadioRow, { key: base, value: base, label: BASE_NAMES[base] }))),
            entry.builtIn ? null : h(TableRowGroup, { title: "Palettes", description: "A palette replaces one family of Discord's colors, tone for tone." },
                ...Object.keys(PALETTE_FAMILIES).map(key => h(TableRow, {
                    key,
                    label: PALETTE_NAMES[key],
                    subLabel: theme.palettes[key] === undefined ? "Discord's colors" : undefined,
                    trailing: theme.palettes[key] === undefined ? null : h(ColorSample, { color: paletteAt(theme.palettes[key], PALETTE_SAMPLE_TONE, 1) }),
                    arrow: true,
                    onPress: () => choosePalette(key),
                }))),
            h(BackgroundSection, { entry }),
            h(TableRowGroup, null,
                h(TableRow, { label: "Share", icon: h(TableRow.Icon, { IconComponent: icons.share }), onPress: () => shareTheme(theme) }),
                h(TableRow, {
                    label: "Duplicate",
                    icon: h(TableRow.Icon, { IconComponent: icons.copy }),
                    onPress: () => navigation.replace(THEME_EDITOR_ROUTE, { themeId: addTheme({ ...theme, name: `${theme.name} copy`, author: null }).id }),
                }),
                entry.builtIn ? null : h(TableRow, {
                    label: "Delete",
                    variant: "danger",
                    icon: h(TableRow.Icon, { IconComponent: icons.trash, variant: "danger" }),
                    onPress: () => design.showSimpleActionSheet({
                        key: "ReseamThemeDelete",
                        header: { title: `Delete ${theme.name}?` },
                        options: [{ label: "Delete theme", isDestructive: true, onPress: () => deleteTheme(entry.id) }],
                    }),
                })),
            h(TableRowGroup, { title: "Colors", description: entry.builtIn ? "Duplicate the theme to change its colors." : "Every color Discord draws with in this base theme." }),
            h(SearchField, { size: "md", placeholder: "Search colors", onChange: setSearch, defaultValue: search }));
    }

    function TokenRow({ entry, token, color, start, end }) {
        const theme = entry.theme;
        const edited = token in theme.semantic;
        const setColor = picked => updateTheme(entry.id, { ...theme, semantic: { ...theme.semantic, [token]: picked } });
        const reset = () => updateTheme(entry.id, { ...theme, semantic: withoutKey(theme.semantic, token) });
        const onPress = entry.builtIn ? undefined : () => {
            if (!edited) return pickColor(color, setColor);
            design.showSimpleActionSheet({
                key: "ReseamThemeColor",
                header: { title: tokenLabel(token), subtitle: token },
                options: [{ label: "Pick a color", onPress: () => pickColor(color, setColor) }, { label: "Use the palette's color", onPress: reset }],
            });
        };
        return h(TableRow, {
            label: tokenLabel(token),
            subLabel: edited ? `${formatColor(color)} · Edited` : formatColor(color),
            trailing: h(ColorSample, { color }),
            start,
            end,
            onPress,
        });
    }

    function ThemeEditorScreen({ navigation, route }) {
        const styles = useStyles();
        react.useSyncExternalStore(subscribe, librarySnapshot);
        const [search, setSearch] = react.useState("");
        const entry = findTheme(route.params.themeId);
        react.useEffect(() => {
            if (entry === null) navigation.goBack();
        }, [entry === null]);
        react.useEffect(() => setPreview(entry), [entry]);
        react.useEffect(() => () => setPreview(null), []);
        if (entry === null) return null;
        const compiled = compiledColors(entry);
        const query = search.trim().toUpperCase().replace(/\s+/g, "_");
        const tokens = Object.keys(compiled).filter(token => token in tables.semantic && token.includes(query)).sort();
        return h(FlatList, {
            contentContainerStyle: styles.screen,
            data: tokens,
            keyExtractor: token => token,
            keyboardShouldPersistTaps: "handled",
            ListHeaderComponent: h(EditorHeader, { entry, navigation, search, setSearch }),
            renderItem: ({ item, index }) => h(TokenRow, { entry, token: item, color: compiled[item], start: index === 0, end: index === tokens.length - 1 }),
        });
    }

    return { ThemesScreen, ThemeEditorScreen };
}

function themeSettingRows(modules) {
    themeScreens ??= createThemeScreens(modules);
    return {
        [THEMES_ROUTE]: {
            type: "route",
            parent: "APPEARANCE",
            IconComponent: modules.icons.paintbrush,
            useTitle: () => "Custom themes",
            useSearchTerms: () => ["Theme", "Colors", "Reseam"],
            screen: { route: THEMES_ROUTE, getComponent: () => themeScreens.ThemesScreen },
        },
        [THEME_EDITOR_ROUTE]: {
            type: "route",
            parent: THEMES_ROUTE,
            unsearchable: true,
            useTitle: () => "Edit theme",
            screen: { route: THEME_EDITOR_ROUTE, getComponent: () => themeScreens.ThemeEditorScreen },
        },
    };
}

// Discord reads rows from this frozen map on every lookup, so a copy with the theme rows replaces it.
exports.settingRows = function (
    react, reactNative, design, colorTokens, colorPicker, checkmark, plus, share, copy, trash, paintbrush, pencil, actions,
    original, global, require, importDefault, importAll, module, moduleExports, dependencyMap,
) {
    setThemeActions(actions);
    const result = original(global, require, importDefault, importAll, module, moduleExports, dependencyMap);
    const icons = {
        checkmark: checkmark.CheckmarkLargeIcon,
        plus: plus.PlusMediumIcon,
        share: share.ShareIcon,
        copy: copy.CopyIcon,
        trash: trash.TrashIcon,
        paintbrush: paintbrush.PaintbrushThinIcon,
        pencil: pencil.PencilIcon,
    };
    const modules = { react, reactNative, design, colorTokens, colorPicker, icons };
    moduleExports.SETTING_RENDERER_CONFIG = Object.freeze({
        ...moduleExports.SETTING_RENDERER_CONFIG,
        ...themeSettingRows(modules),
    });
    return result;
};

exports.appearanceSettings = function (original) {
    const list = original();
    const [theme, ...rest] = list.sections;
    return { ...list, sections: [{ ...theme, settings: [...theme.settings, THEMES_ROUTE] }, ...rest] };
};
