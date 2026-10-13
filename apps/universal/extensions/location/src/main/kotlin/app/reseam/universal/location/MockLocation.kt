// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("MockLocation")

package app.reseam.universal.location

import android.location.Location
import app.reseam.runtime.Export

@Export fun isFromMockProvider(location: Location): Boolean = false

@Export fun isMock(location: Location): Boolean = false
