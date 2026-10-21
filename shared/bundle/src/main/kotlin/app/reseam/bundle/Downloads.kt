// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.bundle

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore

/** Writes a file to the shared Downloads folder without a storage permission. Android 10 and later. */
fun Context.saveToDownloads(name: String, mimeType: String, content: ByteArray) {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    val uri = checkNotNull(contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)) {
        "Downloads refused $name"
    }
    checkNotNull(contentResolver.openOutputStream(uri)).use { it.write(content) }
}
