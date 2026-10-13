// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("FeatureSwitchOverrides")

package app.reseam.x.featureswitches

import app.reseam.bundle.ReseamPatches
import app.reseam.runtime.Export
import app.reseam.runtime.ToggleValue
import java.util.concurrent.ConcurrentHashMap

private val overrides = ConcurrentHashMap<String, ToggleValue>()

@Export
fun disable(key: String, toggleKey: String, toggleDefault: Boolean) {
    overrides[key] = ToggleValue(ReseamPatches.NAME, toggleKey, toggleDefault)
}

/** Null keeps the app's own value. */
@Export fun lookup(key: String): Any? = false.takeIf { overrides[key]?.value == true }
