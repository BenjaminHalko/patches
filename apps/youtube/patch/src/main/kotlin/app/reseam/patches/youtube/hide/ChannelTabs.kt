// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtube.hide

import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.dex.Opcode
import app.reseam.patch.dex.opcode
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.points
import app.reseam.patch.settings.gate
import app.reseam.youtube.hidelayout.bindings.LayoutViews
import app.reseam.youtube.settings.ChannelSettings

internal fun PatchRuntime.hideChannelTabs() {
    val tabTitle = channelTabRenderer.point("channelTabTitle") {
        invokeVirtual { params(Type.String, Type.Boolean); returns(Type.CharSequence) }
    }.writer(1).field()
    val tabModel = channelTabRenderer.point("channelTabModel") {
        opcode(Opcode.IGET_OBJECT)
        field { type(tabTitle.owner) }
    }.field()
    channelTabRenderer.points("channelTabViews") {
        invoke { returns(Type.View); hasParam(Type.CharSequence) }
    }.forEach {
        gate(ChannelSettings.hideChannelTab) {
            next { resultOf(Type.View) }.captureAs("tab", Type.View)
                .after {
                    LayoutViews.hideChannelTabView(capture("tab"), param(0).field(tabModel).field(tabTitle))
                }
        }
    }
}

private val channelTabRenderer = method("channelTabRenderer") {
    strings("Unsupported tabbed view controller insertion.")
    returns(Type.Void)
}
