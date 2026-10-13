// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("SettingsRows")

package app.reseam.x.settings

import app.reseam.runtime.Export

// Built from X's own row models, so the body is emitted by the settings entry patch.
@Export fun section(): Any = error("Replaced by the settings entry patch")

@Export fun single(item: Any): List<Any> = listOf(item)

@Export
fun withSection(sections: List<Any>, section: Any): MutableList<Any> =
    // A data-class copy passes the list back in, and it already has the section.
    if (sections is WithReseamSection) sections
    else WithReseamSection(sections).apply { add(section) }

private class WithReseamSection(sections: List<Any>) : ArrayList<Any>(sections)
