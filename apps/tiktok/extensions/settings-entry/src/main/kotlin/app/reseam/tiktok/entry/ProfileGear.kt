// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

@file:JvmName("ProfileGear")

package app.reseam.tiktok.entry

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import app.reseam.bundle.ReseamPatches
import app.reseam.runtime.Export
import com.bytedance.tux.icon.TuxIconView

private const val TAG = "reseam_settings_gear"
private const val MENU = "com.ss.android.ugc.profile.business.profile.ProfileNavbarMenuProtocol"

/**
 * Puts TikTok's own gear [icon], opening Reseam settings, right before [anchor], once per parent.
 */
@Export
fun attach(anchor: View?, icon: Int) {
    val parent = anchor?.parent as? ViewGroup ?: return
    if (parent.findViewWithTag<View>(TAG) != null) return
    val gear =
        TuxIconView(anchor.context).apply {
            tag = TAG
            contentDescription = "Reseam Settings"
            setIconRes(icon)
            // TikTok's icons draw at the size they are given, so match the anchor's icon.
            (anchor as? ImageView)?.drawable?.let {
                setIconWidth(it.intrinsicWidth)
                setIconHeight(it.intrinsicHeight)
            }
            setTintColor((anchor as? ImageView)?.imageTintList?.defaultColor ?: textColor(anchor))
            setPadding(
                anchor.paddingLeft,
                anchor.paddingTop,
                anchor.paddingRight,
                anchor.paddingBottom,
            )
            setOnClickListener { ReseamPatches.bundle().openSettings() }
        }
    parent.addView(gear, parent.indexOfChild(anchor), copy(anchor.layoutParams))
}

/**
 * [attach] for the signed-in profile's nav bar, where every item reports here and only ≡ gets the
 * gear.
 */
@Export
fun attachToMenu(item: Any?, anchor: View?, icon: Int) {
    if (item?.javaClass?.name == MENU) attach(anchor, icon)
}

private fun copy(params: ViewGroup.LayoutParams) =
    when (params) {
        is LinearLayout.LayoutParams -> LinearLayout.LayoutParams(params)
        is ViewGroup.MarginLayoutParams -> ViewGroup.MarginLayoutParams(params)
        else -> ViewGroup.LayoutParams(params)
    }

private fun textColor(view: View): Int {
    val attributes =
        view.context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
    return attributes.getColor(0, Color.WHITE).also { attributes.recycle() }
}
