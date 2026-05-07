package ru.rbpo.backend.binary;

import ru.rbpo.backend.exception.InvalidSignatureDataException;
import ru.rbpo.backend.model.MalwareSignature;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/** Сборка data.bin: заголовок + записи полезной нагрузки (без id/status/signature). */
public final class SignatureDataBinSerializer {

    private SignatureDataBinSerializer() {}

    public static DataBinBuildResult build(String magicDbPrefix, List<MalwareSignature> records) {
        try {
            List<byte[]> recordChunks = new ArrayList<>();
            for (MalwareSignature sig : records) {
                ByteArrayOutputStream one = new ByteArrayOutputStream();
                writeRecord(one, sig);
                recordChunks.add(one.toByteArray());
            }

            List<Integer> lengths = recordChunks.stream().map(a -> a.length).toList();

            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            for (byte[] chunk : recordChunks) {
                payload.write(chunk);
            }
            byte[] payloadBytes = payload.toByteArray();

            ByteArrayOutputStream full = new ByteArrayOutputStream();
            String magic = "DB-" + magicDbPrefix;
            BinaryEndianWriter.writeUtf8String(full, magic);
            BinaryEndianWriter.writeUInt16(full, SignatureBinaryFormat.DATA_VERSION);
            BinaryEndianWriter.writeUInt32(full, records.size());
            full.write(payloadBytes);

            byte[] fullBytes = full.toByteArray();
            byte[] sha = sha256(fullBytes);
            return new DataBinBuildResult(fullBytes, sha, lengths);
        } catch (IOException e) {
            throw new IllegalStateException("Сериализация data.bin", e);
        }
    }

    public static byte[] sha256(byte[] dataBin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(dataBin);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void writeRecord(ByteArrayOutputStream out, MalwareSignature sig) throws IOException {
        String threat = sig.getThreatName() != null ? sig.getThreatName() : "";
        BinaryEndianWriter.writeUtf8String(out, threat);

        byte[] firstBytes = HexBinaryCodec.decodeHex(sig.getFirstBytesHex(), "firstBytesHex");
        BinaryEndianWriter.writeBytesPrefixed(out, firstBytes);

        byte[] remainderHash = HexBinaryCodec.decodeHex(sig.getRemainderHashHex(), "remainderHashHex");
        BinaryEndianWriter.writeBytesPrefixed(out, remainderHash);

        BinaryEndianWriter.writeInt64(out, sig.getRemainderLength());

        String fileType = sig.getFileType() != null ? sig.getFileType() : "";
        BinaryEndianWriter.writeUtf8String(out, fileType);

        if (sig.getOffsetEnd() < sig.getOffsetStart()) {
            throw new InvalidSignatureDataException("offsetEnd < offsetStart для сигнатуры " + sig.getId());
        }
        BinaryEndianWriter.writeInt64(out, sig.getOffsetStart());
        BinaryEndianWriter.writeInt64(out, sig.getOffsetEnd());
    }
}
