// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.instagram.download

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import app.reseam.bundle.Logger
import app.reseam.bundle.openHttp
import app.reseam.bundle.useReleasing
import kotlin.concurrent.thread

internal fun downloadToSafTree(context: Context, tree: Uri, filename: String, source: MediaUrl) {
    val resolver = context.applicationContext.contentResolver
    thread(name = "reseam-ig-download") {
        runCatching { saveToTree(resolver, tree, filename, source) }
            .onFailure { Logger.error(it) { "SAF download failed for $filename" } }
    }
}

private fun saveToTree(resolver: ContentResolver, tree: Uri, filename: String, source: MediaUrl) {
    val parent =
        DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
    val file =
        DocumentsContract.createDocument(resolver, parent, source.type.mimeType, filename)
            ?: return Logger.error { "createDocument returned null for $filename" }
    val stream =
        resolver.openOutputStream(file)
            ?: return Logger.error { "openOutputStream returned null for $file" }
    // A failed stream close must not turn a completed download into a failure.
    stream.useReleasing({ runCatching { close() } }) { output ->
        openHttp(source.url) {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 30000
            connect()
            val code = responseCode
            if (code in 200..299) {
                inputStream.useReleasing({ runCatching { close() } }) { input ->
                    input.copyTo(output, 16 * 1024)
                    output.flush()
                }
            } else {
                Logger.error { "HTTP $code fetching $filename" }
            }
        }
    }
}
