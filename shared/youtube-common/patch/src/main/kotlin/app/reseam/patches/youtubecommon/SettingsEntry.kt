// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtubecommon

import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.redirectTo
import app.reseam.patch.settings.Reseam

/**
 * Makes a `<Preference>` row whose `<intent>` was set up by
 * [app.reseam.patch.settings.opensSettings] open the bundle's settings when tapped.
 */
fun PatchRuntime.openSettingsFromPreferenceIntents() {
    preferencePerformClick.point { invokeVirtual { name("startActivity") } }
        .redirectTo(Reseam.startActivity)
}

// androidx keeps the class name, but R8 renames performClick.
private val preferencePerformClick = method("preferencePerformClick") {
    inClass(klass("androidx.preference.Preference"))
    params()
    returns(Type.Void)
    calls { owner("android.content.Context"); name("startActivity") }
}
