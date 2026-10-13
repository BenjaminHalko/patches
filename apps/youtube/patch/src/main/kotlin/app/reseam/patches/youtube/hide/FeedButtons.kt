// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.patches.youtube.hide

import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.dex.AccessFlags
import app.reseam.patch.dex.Opcode
import app.reseam.patch.dex.opcode
import app.reseam.patch.dex.typeRef
import app.reseam.patch.method
import app.reseam.patch.point
import app.reseam.patch.settings.gate
import app.reseam.youtube.hidelayout.bindings.LayoutViews
import app.reseam.youtube.settings.FeedSettings

internal fun PatchRuntime.hideFeedButtons() {
    val fab = requireNotNull(resources.id("id", "fab")) { "id/fab is missing" }.toLong()
    val floatingMicrophone = method("floatingMicrophone") {
        flags(AccessFlags.PUBLIC or AccessFlags.FINAL)
        params()
        returns(Type.Void)
        literals(fab)
    }
    gate(FeedSettings.hideFloatingMicrophoneButton) {
        floatingMicrophone.point("floatingMicrophoneResource") { literal(fab) }
            .next {
                opcode(Opcode.CHECK_CAST)
                where { typeRef?.endsWith("/FloatingActionButton;") == true }
            }
            .next { opcode(Opcode.IGET_BOOLEAN) }
            .captureAs("visible", Type.Boolean)
            .after {
            capture("visible").assign(bool(true))
        }
    }

    val contentPill = requireNotNull(resources.id("layout", "content_pill")) {
        "layout/content_pill is missing"
    }.toLong()
    val bar = requireNotNull(resources.id("layout", "bar")) { "layout/bar is missing" }.toLong()
    listOf(
        method("latestVideosContentPill") {
            flags(AccessFlags.PUBLIC or AccessFlags.FINAL)
            paramCount(2)
            returns(Type.Void)
            literals(contentPill)
            calls { name("inflate"); paramCount(3); returns(Type.View) }
        },
        method("latestVideosBar") {
            flags(AccessFlags.PUBLIC or AccessFlags.FINAL)
            paramCount(2)
            returns(Type.Void)
            literals(bar)
            calls { name("inflate"); paramCount(3); returns(Type.View) }
        },
    ).forEach { target ->
        gate(FeedSettings.hideLatestVideosButton) {
            target.point("latestVideosButton") {
                invokeVirtual {
                    name("inflate")
                    paramCount(3)
                    returns(Type.View)
                }
            }
                .next { resultOf(Type.View) }
                .captureAs("view", Type.View)
                .after {
                    LayoutViews.hideLatestVideosButton(capture("view"))
                }
        }
    }

    val youtubeLogo = requireNotNull(resources.id("id", "youtube_logo")) {
        "id/youtube_logo is missing"
    }.toLong()
    val yoodles = method("yoodlesImageView") {
        flags(AccessFlags.PUBLIC or AccessFlags.FINAL)
        paramCount(2)
        returns(Type.View)
        literals(youtubeLogo)
    }
    val setImageDrawable: app.reseam.patch.PointMatch.() -> Unit = {
        invokeVirtual {
            owner("Landroid/widget/ImageView;")
            name("setImageDrawable")
            params("android.graphics.drawable.Drawable")
            returns(Type.Void)
        }
    }
    gate(FeedSettings.hideDoodles) {
        yoodles.point("doodleDrawable", setImageDrawable)
            .captureArgumentAs("image", 0, IMAGE_VIEW)
            .captureArgumentAs("drawable", 1, "android.graphics.drawable.Drawable")
            .before { capture("drawable").assign(nullObject) }
    }
}
