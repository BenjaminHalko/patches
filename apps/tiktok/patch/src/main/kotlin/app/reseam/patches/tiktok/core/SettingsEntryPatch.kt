// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.tiktok.core

import app.reseam.patch.after
import app.reseam.patch.dex.Opcode
import app.reseam.patch.dex.literal
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.tiktok.entry.bindings.ProfileGear

private const val NAVBAR =
    "com.ss.android.ugc.profile.platform.business.navbar.base.NavbarBaseUIComponent"
private const val NAVBAR_ITEM =
    "com.ss.android.ugc.profile.platform.standard.protocol.component.NavbarPlatformProtocol"
private const val ICON_VIEW = "com.bytedance.tux.icon.TuxIconView"

private val signedOutProfile = klass("com.ss.android.ugc.aweme.unlogin.UnloginSignUpFragment")

val settingsGear =
    patch("Reseam settings") {
        description(
            "Adds a gear next to the menu button on the Profile tab that opens Reseam settings."
        )
        compatibleWith(TIKTOK)
        dependsOn(tiktokSettings)

        execute {
            val gear = resources.id("raw", "icon_gear")?.toInt() ?: error("raw/icon_gear missing")
            val menuButton =
                signedOutMenuId.instruction.literal
                    ?: error("No menu button id before the settings route")

            signedOutProfile.method("onViewCreated").after {
                val button =
                    param(0)
                        .callVirtual(
                            "android.view.View",
                            "findViewById",
                            "(I)Landroid/view/View;",
                            int(menuButton.toInt()),
                        )
                call(ProfileGear.attach, button, int(gear))
            }
            navbarIconSetup.after {
                val navbar = thisObject.fieldOfType(NAVBAR)
                call(
                    ProfileGear.attachToMenu,
                    navbar.call(navbarItem),
                    navbar.call(navbarIcon),
                    int(gear),
                )
            }
        }
    }

// The signed-out profile's ≡ opens Settings and privacy; its id is the one compared just before
// that route.
private val signedOutMenuId =
    signedOutProfile
        .method("onClick")
        .point { string("//setting") }
        .previous { opcode(Opcode.CONST) }

private val navbarIconSetup =
    method("navbarIconSetup") { strings("updateComponentUIAndData v3: updateNavActionByStandard") }

private val navbarItem =
    method("navbarItem") {
        inClass(klass(NAVBAR))
        paramCount(0)
        returns(NAVBAR_ITEM)
    }

private val navbarIcon =
    method("navbarIcon") {
        inClass(klass(NAVBAR))
        paramCount(0)
        returns(ICON_VIEW)
    }
