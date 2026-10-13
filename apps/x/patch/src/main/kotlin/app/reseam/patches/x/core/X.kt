// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.x.core

import app.reseam.patch.CompatiblePackage
import app.reseam.patch.invoke

const val X_PACKAGE = "com.twitter.android"

// Pinned: X changes what R8 keeps between releases (12.29 dropped the parameter-name strings older
// targets were anchored on), so targets are read against one release at a time.
val X: CompatiblePackage = X_PACKAGE("12.29.1-prod.01")
