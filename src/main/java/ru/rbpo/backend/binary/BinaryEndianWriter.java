package ru.rbpo.backend.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** Запись примитивов бинарного протокола в network byte order (Big-endian). */
public final class BinaryEndianWriter {

    private BinaryEndianWriter() {}

    public static void writeUInt16(ByteArrayOutputStream out, int value) throws IOException {
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public static void writeUInt32(ByteArrayOutputStream out, long value) throws IOException {
        out.write((int) ((value >>> 24) & 0xFF));
        out.write((int) ((value >>> 16) & 0xFF));
        out.write((int) ((value >>> 8) & 0xFF));
        out.write((int) (value & 0xFF));
    }

    public static void writeInt64(ByteArrayOutputStream out, long value) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(value);
        out.write(bb.array());
    }

    public static void writeUuid(ByteArrayOutputStream out, UUID id) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        bb.putLong(id.getMostSignificantBits());
        bb.putLong(id.getLeastSignificantBits());
        out.write(bb.array());
    }

    /** Строка UTF-8: uint32 (длина в байтах) + байты. */
    public static void writeUtf8String(ByteArrayOutputStream out, String s) throws IOException {
        byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
        writeUInt32(out, utf8.length);
        out.write(utf8);
    }

    /** Сырые байты: uint32 длина + payload. */
    public static void writeBytesPrefixed(ByteArrayOutputStream out, byte[] data) throws IOException {
        if (data == null) {
            writeUInt32(out, 0);
            return;
        }
        writeUInt32(out, data.length);
        out.write(data);
    }

    public static void writeFixed(ByteArrayOutputStream out, byte[] data) throws IOException {
        out.write(data);
    }
}
