// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtubespoof

import app.reseam.patch.CodeScope
import app.reseam.patch.ExtClass
import app.reseam.patch.PatchRuntime
import app.reseam.patch.Type
import app.reseam.patch.ValueRef
import app.reseam.patch.before
import app.reseam.patch.dex.AccessFlags
import app.reseam.patch.dex.Opcode
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.methods
import app.reseam.patch.point
import app.reseam.patch.settings.ToggleSetting
import app.reseam.patch.settings.whenEnabled
import app.reseam.patches.youtubecommon.protobufBytes
import app.reseam.patches.youtubecommon.videoIdGetter

object OfflineStreams : ExtClass("app.reseam.youtube.spoof.OfflineStreams") {
    val replaceRefused by static("[B", returns = "[B")
}

fun PatchRuntime.answerRefusedDownloads(setting: ToggleSetting) {
    fun CodeScope.answer(response: ValueRef, target: ValueRef) = whenEnabled(setting) {
        whenNotNull(response.field(offlineStateField)) {
            whenNull(response.field(streamInputField)) {
                val replaced = call(OfflineStreams.replaceRefused, protobufBytes(response))
                whenNotNull(replaced) {
                    target.assign(parsePlayerResponse(replaced))
                }
            }
        }
    }
    playerResponseReceiver.before {
        whenInstanceOf(param(0), playerResponseClass.descriptor) { answer(param(0).cast(playerResponseClass.descriptor), param(0)) }
    }
    playerResponseModel.methods("player response model constructors") {
        name("<init>")
        param(0, playerResponseClass.descriptor)
    }.forEach { before { answer(param(0), param(0)) } }
}

private val playerResponseReceiver = method("player response receiver") {
    calls(createStreamingData)
    params(Type.Object)
    returns(Type.Void)
}

private val offlineAction = klass("offline state action") { strings("REFRESH_AD", "DELETE_AD") }
private val offlineActionForNumber = method("offline state action lookup") {
    inClass(offlineAction)
    flags(AccessFlags.STATIC)
    params(Type.Int)
}
private val offlineActionCheck = method("offline state action check") {
    flags(AccessFlags.STATIC)
    returns(Type.Boolean)
    params(videoIdGetter.owner)
    calls(offlineActionForNumber)
}
private val offlineStateGetter = offlineActionCheck.point("offline state getter call") {
    invokeInterface { params() }
}.callee("offline state getter")
private val responseOfflineState = method("player response offline state") {
    inClass(playerResponseModel)
    name(offlineStateGetter.name)
    params()
}
internal val offlineStateField = responseOfflineState.point("offline state read") {
    opcode(Opcode.IGET_OBJECT)
    field { owner(playerResponseClass.descriptor) }
}.field("player response offline state field")
