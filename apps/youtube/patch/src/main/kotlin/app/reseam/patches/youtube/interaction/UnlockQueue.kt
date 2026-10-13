// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.patches.youtube.interaction

import app.reseam.patch.CodeScope
import app.reseam.patch.Type
import app.reseam.patch.ValueRef
import app.reseam.patch.after
import app.reseam.patch.before
import app.reseam.patch.classTarget
import app.reseam.patch.dex.AccessFlags
import app.reseam.patch.dex.Opcode
import app.reseam.patch.klass
import app.reseam.patch.method
import app.reseam.patch.patch
import app.reseam.patch.point
import app.reseam.patch.reserveLocal
import app.reseam.patch.settings.section
import app.reseam.patch.settings.whenEnabled
import app.reseam.patches.youtube.core.YOUTUBE
import app.reseam.patches.youtube.core.YouTubeSettingsPages
import app.reseam.patches.youtube.core.youTubeSettings
import app.reseam.patches.youtube.internal.videoInformationHook
import app.reseam.patches.youtubecommon.parseProtobuf
import app.reseam.patches.youtubecommon.protobufByteString
import app.reseam.patches.youtubecommon.protobufByteStringBytes
import app.reseam.patches.youtubecommon.protobufBytes
import app.reseam.youtube.queue.bindings.QueueContext
import app.reseam.youtube.queue.bindings.QueueMenu
import app.reseam.youtube.queue.bindings.QueueReply
import app.reseam.youtube.settings.VideoSettings
import app.reseam.youtube.settings.YouTubeStrings

private const val LISTENABLE_FUTURE = "com.google.common.util.concurrent.ListenableFuture"
private const val OPTIONAL = "j\$.util.Optional"
private const val MESSAGE_LITE = "com.google.protobuf.MessageLite"
private val activeDevicesRequest = method("active devices request") {
    strings("mdx/get_active_devices")
}

private val activeDevicesSender = method("active devices sender") {
    calls(activeDevicesRequest)
    params(Type.String, Type.String)
    returns(LISTENABLE_FUTURE)
}

private val activeDevicesResponse = activeDevicesSender
    .point("active devices response") { invokeVirtual { returns(LISTENABLE_FUTURE) } }
    .next { resultOf(LISTENABLE_FUTURE) }
    .captureAs("response", LISTENABLE_FUTURE)

private val activeDevicesResponseClass = classTarget("active devices response") {
    val defaultResponse = activeDevicesSender
        .point("signed-out response") { invokeStatic { returns(LISTENABLE_FUTURE) } }
        .previous { opcode(Opcode.SGET_OBJECT) }
        .field("active devices default response")
    bytecode.findClass(defaultResponse.type) ?: error("The active devices response class is missing")
}

private val menuCommandResolver = method("menu command resolver") {
    strings("VIDEO_QUALITY_DETAIL_TEXT", "menu_as_bottom_sheet")
    hasParam("java.util.Map")
    returns(Type.Void)
}

private val commandClass = classTarget("command") {
    bytecode.findClass(menuCommandResolver.parameterTypes.first()) ?: error("The command class is missing")
}

private val queueState = klass("queue state") { strings("QP") }

private val queueStateUpdate = method("queue state update") {
    inClass(queueState)
    paramCount(1)
    returns(Type.Void)
    calls { params(); returns(Type.Int) }
}

private val watchNextModel = classTarget("watch next model") {
    bytecode.findClass(queueStateUpdate.parameterTypes.single()) ?: error("The watch next model class is missing")
}

private val watchNextModelInit = watchNextModel.method("<init>") { paramCount(1) }

private val watchNextRequest = klass("watch next request") { strings("watchNextType") }

private val watchNextRequestFactory = method("watch next request factory") {
    flags(AccessFlags.STATIC)
    returns(watchNextRequest.descriptor)
}

private val queueWatchNextRequest = method("queue watch next request") {
    calls(watchNextRequestFactory)
    returns(Type.Void)
}

private val queueInsertResolver = method("queue insert resolver") {
    calls(queueWatchNextRequest)
    hasParam("java.util.Map")
}

private val queueContext = queueInsertResolver
    .point("queue context read") {
        opcode(Opcode.IGET_OBJECT)
        field { owner(queueState.descriptor); type(OPTIONAL) }
    }
    .field("queue context")

val unlockQueue = patch("Unlock queue") {
    description("Unlocks the video queue.")
    compatibleWith(YOUTUBE)
    dependsOn(youTubeSettings, videoInformationHook)
    settings(
        youTubeSettings,
        section(YouTubeSettingsPages.Video, YouTubeStrings.sectionQueue, VideoSettings.unlockQueue),
    )

    execute {
        QueueReply.parse.implement {
            returnValue(parseProtobuf(activeDevicesResponseClass, "active devices response", param(0)))
        }
        val params = activeDevicesSender.reserveLocal("params", Type.String)
        activeDevicesSender.before { local(params).assign(param(1)) }
        activeDevicesResponse.after {
            whenEnabled(VideoSettings.unlockQueue) {
                capture("response").assign(QueueReply.wrap(capture("response"), local(params)))
            }
        }

        menuCommandResolver.before {
            whenEnabled(VideoSettings.unlockQueue) {
                val unlocked = QueueMenu.unlock(protobufBytes(param(0)))
                whenNotNull(unlocked) { param(0).assign(parseProtobuf(commandClass, "command", unlocked)) }
            }
        }

        watchNextModelInit.after {
            whenEnabled(VideoSettings.unlockQueue) {
                QueueContext.watched(thisObject.fieldOfType(queueInsertResolver.parameterTypes.first()))
            }
        }
        val current = queueInsertResolver.reserveLocal("queue context", "[B")
        queueInsertResolver.before {
            whenEnabled(VideoSettings.unlockQueue) {
                val state = thisObject.fieldOfType(queueState.descriptor)
                val stored = state.field(queueContext)
                local(current).assign(nullObject)
                whenTrue(stored.callVirtual(OPTIONAL, "isPresent", "()Z")) {
                    local(current).assign(protobufByteStringBytes(stored.callVirtual(OPTIONAL, "get", "()Ljava/lang/Object;")))
                }
                val completed = QueueContext.complete(local(current))
                whenNotNull(completed) { state.set(queueContext, optionalOf(protobufByteString(completed))) }
            }
        }
    }
}

private fun CodeScope.optionalOf(value: ValueRef) =
    callStatic(OPTIONAL, "of", "(Ljava/lang/Object;)L${OPTIONAL.replace('.', '/')};", value)
