// SPDX-FileCopyrightText: 2026 ReVanced contributors
// SPDX-License-Identifier: GPL-3.0-or-later

package app.reseam.youtube.dislike.requests

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.math.pow
import org.json.JSONObject

internal fun solve(puzzle: JSONObject): String {
    val challenge = puzzle.getString("challenge")
    val difficulty = puzzle.getInt("difficulty")
    val decoded = Base64.decode(challenge, Base64.NO_WRAP)
    val buffer = ByteArray(20)
    decoded.copyInto(buffer, destinationOffset = 4, endIndex = 16)
    val digest = MessageDigest.getInstance("SHA-512")
    val maxCount = (2.0.pow(difficulty + 1) * 5).toInt()
    for (i in 0 until maxCount) {
        buffer[0] = i.toByte()
        buffer[1] = (i shr 8).toByte()
        buffer[2] = (i shr 16).toByte()
        buffer[3] = (i shr 24).toByte()
        if (countLeadingZeroes(digest.digest(buffer)) >= difficulty) {
            return Base64.encodeToString(buffer.copyOf(4), Base64.NO_WRAP)
        }
    }
    throw IllegalStateException(
        "Failed to solve puzzle challenge: $challenge difficulty: $difficulty"
    )
}

internal fun randomString(length: Int): String {
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = SecureRandom()
    return buildString(length) {
        repeat(length) { append(alphabet[random.nextInt(alphabet.length)]) }
    }
}

private fun countLeadingZeroes(bytes: ByteArray): Int {
    val first = bytes.indexOfFirst { it.toInt() != 0 }
    return if (first < 0) bytes.size * 8
    else first * 8 + (bytes[first].toInt() and 0xFF).countLeadingZeroBits() - 24
}
