// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("FollowingFeed")

package app.reseam.instagram.feed

import app.reseam.runtime.Export

private const val PAGINATION_SOURCE = "pagination_source"

@Export
fun following(params: Map<*, *>?): MutableMap<Any?, Any?> =
    HashMap<Any?, Any?>(params.orEmpty()).apply {
        val source = this[PAGINATION_SOURCE]
        if (source == null || "feed_recs" == source) this[PAGINATION_SOURCE] = "following"
    }
