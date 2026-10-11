// SPDX-FileCopyrightText: 2026 Cossale <hello@auna.li>
// SPDX-License-Identifier: AGPL-3.0-or-later

package app.reseam.youtube.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.LongUnaryOperator;

/** Bounded protobuf wire operations. Unknown fields are preserved byte-for-byte when replacing fields. */
public final class Proto {
    public static final int WIRE_VARINT = 0;
    public static final int WIRE_FIXED64 = 1;
    public static final int WIRE_LENGTH = 2;
    public static final int WIRE_FIXED32 = 5;

    private Proto() {}

    public static byte[] bytes(byte[] message, int number) {
        byte[] value = null;
        for (Field field : fields(message)) {
            if (field.number == number && field.wireType == WIRE_LENGTH) value = field.bytes;
        }
        return value;
    }

    public static String string(byte[] message, int number) {
        byte[] value = bytes(message, number);
        return value == null ? null : new String(value, StandardCharsets.UTF_8);
    }

    public static long number(byte[] message, int number, long fallback) {
        long value = fallback;
        for (Field field : fields(message)) {
            if (field.number == number && field.wireType == WIRE_VARINT) value = field.varint;
        }
        return value;
    }

    public static byte[] replace(byte[] message, Map<Integer, byte[]> replacements) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(message.length);
        for (Field field : fields(message)) {
            if (!replacements.containsKey(field.number)) output.write(message, field.start, field.end - field.start);
        }
        replacements.forEach((number, value) -> {
            if (value != null) writeBytes(output, number, value);
        });
        return output.toByteArray();
    }

    public interface LengthRewrite {
        byte[] apply(int number, byte[] value);
    }

    public static byte[] rewriteLength(byte[] message, LengthRewrite rewrite) {
        List<Field> fields = fields(message);
        ByteArrayOutputStream output = null;
        for (int index = 0; index < fields.size(); index++) {
            Field field = fields.get(index);
            byte[] value = field.wireType == WIRE_LENGTH ? rewrite.apply(field.number, field.bytes) : field.bytes;
            if (output == null && value != field.bytes) {
                output = new ByteArrayOutputStream(message.length);
                output.write(message, 0, field.start);
            }
            if (output == null) continue;
            if (value == field.bytes) output.write(message, field.start, field.end - field.start);
            else if (value != null) writeBytes(output, field.number, value);
        }
        return output == null ? message : output.toByteArray();
    }

    public static byte[] rewriteNumber(byte[] message, int number, LongUnaryOperator rewrite) {
        List<Field> fields = fields(message);
        ByteArrayOutputStream output = null;
        for (Field field : fields) {
            long value = field.number == number && field.wireType == WIRE_VARINT
                    ? rewrite.applyAsLong(field.varint) : field.varint;
            if (output == null && value != field.varint) {
                output = new ByteArrayOutputStream(message.length);
                output.write(message, 0, field.start);
            }
            if (output == null) continue;
            if (value != field.varint) writeNumber(output, field.number, value);
            else output.write(message, field.start, field.end - field.start);
        }
        return output == null ? message : output.toByteArray();
    }

    /** Builds a message field by field, in the order written. */
    public static final class Writer {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();

        public Writer number(int number, long value) {
            writeNumber(output, number, value);
            return this;
        }

        public Writer bytes(int number, byte[] value) {
            writeBytes(output, number, value);
            return this;
        }

        public Writer string(int number, String value) {
            return bytes(number, value.getBytes(StandardCharsets.UTF_8));
        }

        public Writer message(int number, Writer message) {
            return bytes(number, message.toByteArray());
        }

        public byte[] toByteArray() {
            return output.toByteArray();
        }
    }

    public static void writeNumber(ByteArrayOutputStream output, int number, long value) {
        writeVarint(output, (long) number << 3);
        writeVarint(output, value);
    }

    public static final class Field {
        public final int number;
        public final int wireType;
        public final long varint;
        public final byte[] bytes;
        /** The field's encoded span in its message, tag included. */
        public final int start;
        public final int end;

        Field(int number, int wireType, long varint, byte[] bytes, int start, int end) {
            this.number = number;
            this.wireType = wireType;
            this.varint = varint;
            this.bytes = bytes;
            this.start = start;
            this.end = end;
        }

        public String string() {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static List<Field> fields(byte[] message) {
        List<Field> fields = new ArrayList<>();
        int[] position = {0};
        while (position[0] < message.length) {
            int start = position[0];
            long tag = readVarint(message, position);
            if (tag <= 0 || (tag >>> 3) > 0x1FFFFFFFL || (tag >>> 3) == 0) {
                throw new IllegalArgumentException("Invalid protobuf tag");
            }
            int number = (int) (tag >>> 3);
            int wireType = (int) (tag & 7);
            long varint = 0;
            byte[] bytes = null;
            switch (wireType) {
                case WIRE_VARINT:
                    varint = readVarint(message, position);
                    break;
                case WIRE_FIXED64:
                    position[0] += 8;
                    break;
                case WIRE_LENGTH:
                    long encodedLength = readVarint(message, position);
                    if (encodedLength < 0 || encodedLength > message.length - position[0]) {
                        throw new IllegalArgumentException("Truncated protobuf field " + number);
                    }
                    int length = (int) encodedLength;
                    bytes = new byte[length];
                    System.arraycopy(message, position[0], bytes, 0, length);
                    position[0] += length;
                    break;
                case WIRE_FIXED32:
                    position[0] += 4;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported protobuf wire type " + wireType);
            }
            if (position[0] > message.length) throw new IllegalArgumentException("Truncated protobuf message");
            fields.add(new Field(number, wireType, varint, bytes, start, position[0]));
        }
        return fields;
    }

    public static long readVarint(byte[] message, int[] position) {
        long value = 0;
        for (int shift = 0; shift < 64; shift += 7) {
            if (position[0] >= message.length) throw new IllegalArgumentException("Truncated protobuf varint");
            byte b = message[position[0]++];
            if (shift == 63 && (b & 0xFE) != 0) throw new IllegalArgumentException("Malformed protobuf varint");
            value |= (long) (b & 0x7F) << shift;
            if (b >= 0) return value;
        }
        throw new IllegalArgumentException("Malformed protobuf varint");
    }

    public static void writeBytes(ByteArrayOutputStream output, int number, byte[] bytes) {
        writeVarint(output, ((long) number << 3) | WIRE_LENGTH);
        writeVarint(output, bytes.length);
        output.write(bytes, 0, bytes.length);
    }

    public static void writeVarint(ByteArrayOutputStream output, long value) {
        while ((value & ~0x7FL) != 0) {
            output.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        output.write((int) value);
    }
}
