// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("MediaMeta")

package app.reseam.instagram.download

import app.reseam.runtime.Export

@Export fun username(media: Any?): String? = error("Replaced by the download patch")

@Export fun reelItemMedia(reelItem: Any): Any? = error("Replaced by the download patch")

@Export fun storyOwnerReelItem(owner: Any): Any? = error("Replaced by the download patch")

@Export fun storyOwnerContext(owner: Any): Any? = error("Replaced by the download patch")
