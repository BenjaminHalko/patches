// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.bundle

import app.reseam.runtime.Reseam
import app.reseam.runtime.ReseamBundle

object ReseamPatches {
    const val NAME = "reseam-patches"

    fun bundle(): ReseamBundle = Reseam.bundle(NAME)
}
