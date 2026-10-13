// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("Media")

package app.reseam.instagram.refs

import app.reseam.runtime.Export

@Export
fun photoUrl(media: Any): String? =
    imageCandidates(media)?.firstNotNullOfOrNull { candidate ->
        imageCandidateUrl(candidate)?.takeIf { it.isNotEmpty() }
    }

@Export fun imageCandidates(media: Any): List<Any>? = error("Replaced by the media refs patch")

@Export fun imageCandidateUrl(candidate: Any): String? = error("Replaced by the media refs patch")

@Export fun videoUrl(media: Any): String? = error("Replaced by the media refs patch")

@Export fun children(media: Any): List<*>? = error("Replaced by the media refs patch")
