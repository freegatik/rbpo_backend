package ru.rbpo.backend.binary;

import ru.rbpo.backend.model.MalwareSignature;
import ru.rbpo.backend.model.SignatureStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/** Неподписанная часть manifest.bin (до manifestSignatureLength / manifestSignatureBytes). */
public final class ManifestBinSerializer {

    private ManifestBinSerializer() {}

    public static byte[] buildUnsignedManifest(
            String magicMfPrefix,
            int exportType,
            long generatedAtEpochMillis,
            long sinceEpochMillis,
            byte[] dataSha256,
            List<MalwareSignature> records,
            List<Integer> recordPayloadLengths,
            List<byte[]> recordSignatureBytes) {

        if (records.size() != recordPayloadLengths.size() || records.size() != recordSignatureBytes.size()) {
            throw new IllegalArgumentException("Размеры списков манифеста не совпадают");
        }

        try {
            ByteArrayOutputStream unsigned = new ByteArrayOutputStream();

            String magic = "MF-" + magicMfPrefix;
            BinaryEndianWriter.writeUtf8String(unsigned, magic);
            BinaryEndianWriter.writeUInt16(unsigned, SignatureBinaryFormat.MANIFEST_VERSION);
            unsigned.write(exportType & 0xFF);
            BinaryEndianWriter.writeInt64(unsigned, generatedAtEpochMillis);
            BinaryEndianWriter.writeInt64(unsigned, sinceEpochMillis);
            BinaryEndianWriter.writeUInt32(unsigned, records.size());
            BinaryEndianWriter.writeFixed(unsigned, dataSha256);

            long offset = 0;
            for (int i = 0; i < records.size(); i++) {
                MalwareSignature sig = records.get(i);
                BinaryEndianWriter.writeUuid(unsigned, sig.getId());
                unsigned.write(toStatusCode(sig.getStatus()));
                BinaryEndianWriter.writeInt64(unsigned, sig.getUpdatedAt().toEpochMilli());
                BinaryEndianWriter.writeInt64(unsigned, offset);
                int len = recordPayloadLengths.get(i);
                BinaryEndianWriter.writeUInt32(unsigned, len & 0xFFFFFFFFL);
                byte[] sigBytes = recordSignatureBytes.get(i);
                BinaryEndianWriter.writeUInt32(unsigned, sigBytes.length);
                BinaryEndianWriter.writeFixed(unsigned, sigBytes);
                offset += len;
            }

            return unsigned.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Сериализация manifest.bin", e);
        }
    }

    /** Дописывает manifestSignatureLength (uint32 BE) и manifestSignatureBytes. */
    public static byte[] appendManifestSignature(byte[] unsignedManifest, byte[] manifestSignature) {
        try {
            ByteArrayOutputStream full = new ByteArrayOutputStream();
            full.write(unsignedManifest);
            BinaryEndianWriter.writeUInt32(full, manifestSignature.length);
            full.write(manifestSignature);
            return full.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte toStatusCode(SignatureStatus status) {
        if (status == SignatureStatus.ACTUAL) {
            return SignatureBinaryFormat.STATUS_ACTUAL;
        }
        if (status == SignatureStatus.DELETED) {
            return SignatureBinaryFormat.STATUS_DELETED;
        }
        return SignatureBinaryFormat.STATUS_ACTUAL;
    }

    public static byte[] decodeRecordSignatureBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new ru.rbpo.backend.exception.InvalidSignatureDataException(
                    "digitalSignatureBase64 отсутствует — запись не может быть экспортирована в binary API");
        }
        try {
            return Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new ru.rbpo.backend.exception.InvalidSignatureDataException("Некорректный Base64 подписи записи");
        }
    }
}
