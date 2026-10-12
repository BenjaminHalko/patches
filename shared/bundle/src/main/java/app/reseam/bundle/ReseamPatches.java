// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.bundle;

import app.reseam.runtime.Reseam;
import app.reseam.runtime.ReseamBundle;

public final class ReseamPatches {
    /** The bundle's name in {@code manifest.toml}. */
    public static final String NAME = "reseam-patches";

    private ReseamPatches() {}

    public static ReseamBundle bundle() {
        return Reseam.bundle(NAME);
    }
}
