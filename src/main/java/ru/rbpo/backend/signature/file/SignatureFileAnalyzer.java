package ru.rbpo.backend.signature.file;

import ru.rbpo.backend.exception.InvalidSignatureDataException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/** Расчёт полей сигнатуры по содержимому файла (первые 64 байта + SHA-256 хвоста). */
public final class SignatureFileAnalyzer {

    private static final int PREFIX_BYTES = 64;
    private static final HexFormat HEX = HexFormat.of().withLowerCase();

    private SignatureFileAnalyzer() {}

    public record DerivedFields(
            String threatName,
            String firstBytesHex,
            String remainderHashHex,
            long remainderLength,
            String fileType,
            long offsetStart,
            long offsetEnd
    ) {}

    public static DerivedFields derive(byte[] fileBytes, String originalFilename,
                                       String threatNameOverride, String contentTypeHint) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new InvalidSignatureDataException("Файл пустой");
        }
        int prefixLen = Math.min(PREFIX_BYTES, fileBytes.length);
        byte[] prefix = new byte[prefixLen];
        System.arraycopy(fileBytes, 0, prefix, 0, prefixLen);

        int remLen = fileBytes.length - prefixLen;
        byte[] remainder = new byte[remLen];
        if (remLen > 0) {
            System.arraycopy(fileBytes, prefixLen, remainder, 0, remLen);
        }

        String firstHex = HEX.formatHex(prefix);
        String remainderHashHex = sha256Hex(remainder);

        String fileType = inferFileType(originalFilename, contentTypeHint);
        String threat = (threatNameOverride != null && !threatNameOverride.isBlank())
                ? threatNameOverride.trim()
                : inferThreatName(originalFilename);

        return new DerivedFields(
                threat,
                firstHex,
                remainderHashHex,
                remLen,
                fileType,
                0L,
                (long) prefixLen - 1L
        );
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HEX.formatHex(md.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String inferThreatName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "uploaded.sample";
        }
        String base = originalFilename.trim();
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            return base.substring(0, dot);
        }
        return base.isEmpty() ? "uploaded.sample" : base;
    }

    private static String inferFileType(String originalFilename, String contentTypeHint) {
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase(Locale.ROOT);
            int dot = lower.lastIndexOf('.');
            if (dot >= 0 && dot < lower.length() - 1) {
                return lower.substring(dot + 1);
            }
        }
        if (contentTypeHint != null && contentTypeHint.contains("/")) {
            String sub = contentTypeHint.substring(contentTypeHint.indexOf('/') + 1);
            if (!sub.isBlank() && sub.length() <= 32) {
                return sub.replaceAll("[^a-zA-Z0-9.-]", "");
            }
        }
        return "bin";
    }

    public static String sanitizeStorageFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "upload.bin";
        }
        String base = originalFilename.trim();
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        String cleaned = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank()) {
            cleaned = "upload.bin";
        }
        return cleaned.length() > 200 ? cleaned.substring(0, 200) : cleaned;
    }
}
