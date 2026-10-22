// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.x.core

import app.reseam.patch.CodeScope
import app.reseam.patch.MethodQuery
import app.reseam.patch.MethodsTarget
import app.reseam.patch.Type
import app.reseam.patch.ValueRef
import app.reseam.patch.before
import app.reseam.patch.descriptor
import app.reseam.patch.dex.ref
import app.reseam.patch.fieldTarget
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.settings.openSettings
import app.reseam.x.settings.bindings.OpenReseamSettings
import app.reseam.x.settings.bindings.SettingsRows

private const val FUNCTION0 = "kotlin.jvm.functions.Function0"
private const val LOGO_PATH = "res/drawable/reseam_logo.png"

private const val SECTION_DEFAULTS_HEADER_AND_SUBTITLE = 0b11

// The root page builds its title and reads its sections right before constructing its state.
private const val ROOT_TITLE_TO_STATE = 16

val settingsEntry = patch("Reseam entry in X settings") {
    description("Adds a Reseam Settings row to X's Settings and privacy screen.")
    compatibleWith(X)
    dependsOn(xSettings)

    execute {
        val logo = Resources::class.java.getResourceAsStream("/reseam-logo.png")?.use { it.readBytes() }
            ?: error("reseam-logo.png missing from patch jar resources")
        val logoId = resources.addFile("drawable", "reseam_logo", LOGO_PATH, logo).toInt()

        // R8 renames kotlin.Unit.INSTANCE, so the Function0 body is emitted here.
        OpenReseamSettings.invoke.implement {
            openSettings()
            returnValue(staticField(unitInstance))
        }

        val settingsTitle = resources.id("string", "settings") ?: error("string/settings missing")
        // 12.34 ships a redesign beside the legacy screen, picked per account by the
        // android_settings_redesign_enabled feature switch, so every design gets the row.
        for (design in settingsDesigns()) {
            val rootSettings = method("rootSettings:${design.packageName}") {
                literals(settingsTitle.toLong())
                calls(design.stateCtor)
            }
            // The screen copies its state while filtering rows.
            val sectionsArgument = design.stateCtor.parameterTypes.indexOf(Type.List) + 1
            rootSettings.point {
                literal(settingsTitle.toLong())
                then(within = ROOT_TITLE_TO_STATE) { calls(design.stateCtor) }
            }.captureArgumentAs("sections", sectionsArgument, Type.List)
                .before {
                    val sections = capture("sections")
                    sections.assign(SettingsRows.withSection(sections, reseamSection(design, logoId)))
                }
        }
    }
}

/** One settings design: X keeps each design's state, section and row models together in a package. */
private class SettingsDesign(val packageName: String) {
    val stateCtor = model(settingsListStates).method("<init>")
    // Kotlin's defaults constructors, the ones R8 keeps in every design.
    val sectionCtor = model(settingsListSections).method("<init>") { hasParam(Type.List); defaultsMask() }
    val itemCtor = model(settingsListItems).method("<init>") { hasParam(FUNCTION0); defaultsMask() }

    // The literal variant of the text type the rows take.
    val literalTextCtor = klass("literalText:$packageName") {
        strings("Literal(text=")
        extends(itemCtor.parameterTypes[0])
    }.method("<init>") { params(Type.String) }

    private fun model(toStrings: MethodsTarget) =
        klass(toStrings.all.single { it.owner.substringBeforeLast('/') == packageName }.owner)
}

private fun MethodQuery.defaultsMask() = custom { parameterTypes.last() == Type.Int }

private fun settingsDesigns() =
    settingsListStates.all.map { SettingsDesign(it.owner.substringBeforeLast('/')) }

private val settingsListStates = methods("settingsListState.toString") {
    name("toString")
    strings("SettingsListState(onBackClicked=")
}
private val settingsListSections = methods("settingsListSection.toString") {
    name("toString")
    strings("SettingsListSection(title=")
}
private val settingsListItems = methods("settingsListItem.toString") {
    name("toString")
    strings("SettingsListItem(title=")
}

private fun CodeScope.reseamSection(design: SettingsDesign, logoId: Int): ValueRef {
    val text = design.literalTextCtor
    val title = newInstance(text.owner, text.proto, string("Reseam Settings"))
    val subtitle = newInstance(text.owner, text.proto, string("Ads, downloads, Premium, Grok, and privacy toggles."))
    val item = design.itemCtor
    // R8 drops constructor parameters without renumbering the default bits, so pass every default.
    val arguments = listOf(title, subtitle, newInstance(item.parameterTypes[2], "(I)V", int(logoId))) +
        item.parameterTypes.drop(3).map { type ->
            when (type) {
                descriptor(FUNCTION0) -> newInstance(OpenReseamSettings.descriptor)
                Type.Boolean -> bool(false)
                Type.Int -> int(0)
                else -> nullObject
            }
        }
    val section = design.sectionCtor
    return newInstance(
        section.owner, section.proto,
        nullObject, nullObject,
        SettingsRows.single(newInstance(item.owner, item.proto, *arguments.toTypedArray())),
        int(SECTION_DEFAULTS_HEADER_AND_SUBTITLE),
    )
}

private object Resources

val unitInstance = fieldTarget("unitInstance") {
    val unit = bytecode.findClass("kotlin.Unit") ?: error("kotlin.Unit missing")
    unit.staticFields.single { it.fieldType == descriptor("kotlin.Unit") }.ref
}
