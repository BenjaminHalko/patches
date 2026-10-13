// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.core

import app.reseam.youtube.core.Proto.Field
import app.reseam.youtube.core.Proto.WIRE_FIXED32
import app.reseam.youtube.core.Proto.WIRE_FIXED64
import app.reseam.youtube.core.Proto.WIRE_LENGTH
import app.reseam.youtube.core.Proto.WIRE_VARINT
import java.io.ByteArrayOutputStream

internal class ProtoReader(private val message: ByteArray) {
    private var position = 0

    val hasMore: Boolean
        get() = position < message.size

    fun field(): Field {
        val start = position
        val tag = varint()
        val number = tag ushr 3
        require(number in 1..0x1FFFFFFFL) { "Invalid protobuf tag" }
        val wireType = (tag and 7).toInt()
        var varint = 0L
        var bytes: ByteArray? = null
        when (wireType) {
            WIRE_VARINT -> varint = varint()
            WIRE_FIXED64 -> skip(8)
            WIRE_LENGTH -> bytes = lengthDelimited()
            WIRE_FIXED32 -> skip(4)
            else -> throw IllegalArgumentException("Unsupported protobuf wire type $wireType")
        }
        return Field(number.toInt(), wireType, varint, bytes, start, position)
    }

    fun varint(): Long {
        var value = 0L
        for (shift in 0 until 64 step 7) {
            require(hasMore) { "Truncated protobuf varint" }
            val byte = message[position++].toInt()
            require(shift != 63 || (byte and 0xFE) == 0) { "Malformed protobuf varint" }
            value = value or ((byte and 0x7F).toLong() shl shift)
            if (byte >= 0) return value
        }
        throw IllegalArgumentException("Malformed protobuf varint")
    }

    private fun lengthDelimited(): ByteArray {
        val length = varint()
        require(length in 0..message.size - position) { "Truncated protobuf field" }
        return message.copyOfRange(position, position + length.toInt()).also { skip(it.size) }
    }

    private fun skip(count: Int) {
        position += count
        require(position <= message.size) { "Truncated protobuf message" }
    }
}

internal fun ByteArrayOutputStream.writeNumber(number: Int, value: Long) {
    writeVarint(number.toLong() shl 3)
    writeVarint(value)
}

internal fun ByteArrayOutputStream.writeBytes(number: Int, bytes: ByteArray) {
    writeVarint((number.toLong() shl 3) or WIRE_LENGTH.toLong())
    writeVarint(bytes.size.toLong())
    write(bytes, 0, bytes.size)
}

internal fun ByteArrayOutputStream.writeVarint(value: Long) {
    var remaining = value
    while ((remaining and 0x7FL.inv()) != 0L) {
        write(((remaining and 0x7F) or 0x80).toInt())
        remaining = remaining ushr 7
    }
    write(remaining.toInt())
}
