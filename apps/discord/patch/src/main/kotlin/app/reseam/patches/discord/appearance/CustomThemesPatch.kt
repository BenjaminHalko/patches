// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.discord.appearance

import app.reseam.discord.settings.DiscordSettings
import app.reseam.discord.settings.DiscordStrings
import app.reseam.discord.themes.bindings.ThemeColors
import app.reseam.discord.themes.bindings.ThemesPackage
import app.reseam.patch.ExtJsModule
import app.reseam.patch.MetroModule
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.appEntry
import app.reseam.patch.function
import app.reseam.patch.klass
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.settings.gate
import app.reseam.patch.settings.section
import app.reseam.patch.settings.whenEnabled
import app.reseam.patches.discord.core.DISCORD
import app.reseam.patches.discord.core.commit
import app.reseam.patches.discord.core.createMessageContent
import app.reseam.patches.discord.core.designSystem
import app.reseam.patches.discord.core.discordSettings
import app.reseam.patches.discord.core.getOrCreate
import app.reseam.patches.discord.core.getPackages
import app.reseam.patches.discord.core.react
import app.reseam.patches.discord.core.reactNative
import app.reseam.patches.discord.core.settingRows

val customThemes = patch("Custom themes") {
    description("Create, edit, share and use full themes, the Reseam theme included.")
    compatibleWith(DISCORD)
    settings(discordSettings, section(DiscordStrings.sectionAppearance, DiscordSettings.customThemes))

    execute {
        gate(DiscordSettings.customThemes) {
            colorTokensModule.wrap(CustomThemes.colorTokensModule)
            colorExperimentsModule.wrap(CustomThemes.colorExperimentsModule)
            rootThemeContextProvider.wrap(CustomThemes.RootThemeContextProvider, react)
            semanticColorContext.wrap(CustomThemes.getSemanticColorContextFromThemeContext)
            createNativeStyleProperties.wrap(CustomThemes.createNativeStyleProperties)
            resolveTheme.wrap(CustomThemes.resolveTheme)
            useColorThemeBackground.wrap(CustomThemes.useColorThemeBackground, react)
            getCustomBackgroundGradient.wrap(CustomThemes.getCustomBackgroundGradient)
            gradientPreset.wrap(CustomThemes.gradientPreset)
            useActiveThemeType.wrap(CustomThemes.useActiveThemeType, react)
            useCustomThemeDisplaySettings.wrap(CustomThemes.useCustomThemeDisplaySettings, react)
            useRoutedActiveGuildTheme.wrap(CustomThemes.useRoutedActiveGuildTheme, react)
            settingRows.wrap(
                CustomThemes.settingRows,
                react,
                reactNative,
                designSystem,
                colorTokens,
                colorPicker,
                checkmarkIcon,
                plusIcon,
                shareIcon,
                copyIcon,
                trashIcon,
                paintbrushIcon,
                pencilIcon,
                themeActions,
            )
            appearanceSettings.wrap(CustomThemes.appearanceSettings)
            getOrCreate.wrap(CustomThemes.getOrCreate)
            commit.wrap(CustomThemes.commit)
            createMessageContent.wrap(CustomThemes.createMessageContent, messageStore)
            formatSharedClientThemeData.wrap(CustomThemes.formatSharedClientThemeData)
            handleTapPreviewSharedClientTheme.wrap(CustomThemes.handleTapPreviewSharedClientTheme, themeActions, designSystem, react, colorTokens, actionSheets)
            getPackages.after { ThemesPackage.addTo(capture("result")) }
        }
        appEntry { whenEnabled(DiscordSettings.customThemes) { ThemeColors.init(application) } }
        // One getter per semantic token in each of Discord's native theme classes.
        for ((themeClass, theme) in nativeThemes) {
            klass(themeClass).methods { returns(Type.Int); paramCount(0) }.forEach {
                val token = nativeToken(name)
                after { returnValue(ThemeColors.color(string(theme), string(token), capture("result"))) }
            }
        }
    }
}

private object CustomThemes : ExtJsModule("discord-custom-themes") {
    val colorTokensModule by export()
    val colorExperimentsModule by export()
    val RootThemeContextProvider by export()
    val getSemanticColorContextFromThemeContext by export()
    val createNativeStyleProperties by export()
    val resolveTheme by export()
    val useColorThemeBackground by export()
    val getCustomBackgroundGradient by export()
    val gradientPreset by export()
    val useActiveThemeType by export()
    val useCustomThemeDisplaySettings by export()
    val useRoutedActiveGuildTheme by export()
    val settingRows by export()
    val appearanceSettings by export()
    val getOrCreate by export()
    val commit by export()
    val createMessageContent by export()
    val formatSharedClientThemeData by export()
    val handleTapPreviewSharedClientTheme by export()
}

private val nativeThemes = listOf(
    "com.discord.theme.AshTheme" to "dark",
    "com.discord.theme.DarkTheme" to "darker",
    "com.discord.theme.LightTheme" to "light",
    "com.discord.theme.OnyxTheme" to "midnight",
)

private fun nativeToken(getter: String) =
    getter.removePrefix("get").replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "_").uppercase()

private val colorTokensModule = function {
    name("")
    strings("../discord_common/js/packages/tokens/native.tsx")
    paramCount(7)
}

private val colorExperimentsModule = function {
    name("")
    strings("../discord_common/js/packages/tokens/colors/generated/native/experiment-definitions.tsx")
    paramCount(7)
}

private val colorTokens: MetroModule = function {
    name("sanitizeTheme")
    strings("Invalid theme: ")
    paramCount(1)
}.module

private val rootThemeContextProvider = function {
    name("RootThemeContextProvider")
    strings("reduceAdaptiveTheme", "ThemeContext")
    paramCount(1)
}

private val semanticColorContext = function {
    name("getSemanticColorContextFromThemeContext")
    paramCount(1)
}

private val createNativeStyleProperties = function {
    name("createNativeStyleProperties")
    paramCount(1)
}

private val resolveTheme = function {
    name("resolveTheme")
    strings("getPreviewTheme", "resolveThemeWithCustomSettings")
    paramCount(2)
}

private val useColorThemeBackground = function {
    name("useColorThemeBackground")
    paramCount(0)
}

private val getCustomBackgroundGradient = function {
    name("getCustomBackgroundGradient")
    paramCount(0)
}

private val gradientPreset = function {
    name("gradientPreset")
    paramCount(0)
}

private val useActiveThemeType = function {
    name("useActiveThemeType")
    paramCount(0)
}

private val useCustomThemeDisplaySettings = function {
    name("useCustomThemeDisplaySettings")
    paramCount(1)
}

private val useRoutedActiveGuildTheme = function {
    name("useRoutedActiveGuildTheme")
    paramCount(0)
}

private val themeActions: MetroModule = function {
    name("refreshTheme")
    paramCount(0)
}.module

private val actionSheets: MetroModule = function {
    name("hideAllActionSheets")
    paramCount(0)
}.module

private val colorPicker: MetroModule = function {
    name("showCustomColorPickerActionSheet")
    paramCount(2)
}.module

private val checkmarkIcon = icon("CheckmarkLargeIcon")
private val plusIcon = icon("PlusMediumIcon")
private val shareIcon = icon("ShareIcon")
private val copyIcon = icon("CopyIcon")
private val trashIcon = icon("TrashIcon")
private val paintbrushIcon = icon("PaintbrushThinIcon")
private val pencilIcon = icon("PencilIcon")

private fun icon(name: String): MetroModule = function {
    name(name)
    paramCount(1)
}.module

private val appearanceSettings = function {
    name("")
    strings("SAME_AS_DEVICE_THEME", "APPEARANCE_THEME_PICKER", "DMS_HAPPENING_NOW_CARDS")
    paramCount(0)
}

private val messageStore: MetroModule = function {
    name("hasCurrentUserSentMessageSinceAppStart")
    paramCount(0)
}.module

private val formatSharedClientThemeData = function {
    name("formatSharedClientThemeData")
    strings("sharedClientTheme")
    paramCount(3)
}

private val handleTapPreviewSharedClientTheme = function {
    name("handleTapPreviewSharedClientTheme")
    strings("custom-theme-preview")
    paramCount(1)
}
