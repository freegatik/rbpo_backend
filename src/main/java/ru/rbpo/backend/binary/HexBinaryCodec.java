package ru.rbpo.backend.binary;

import ru.rbpo.backend.exception.InvalidSignatureDataException;

/** Декодирование hex-строк полей сигнатуры в сырые байты для data.bin. */
public final class HexBinaryCodec {

    private HexBinaryCodec() {}

    public static byte[] decodeHex(String hex, String fieldName) {
        if (hex == null || hex.isBlank()) {
            throw new InvalidSignatureDataException(fieldName + ": пустое hex-значение");
        }
        String s = hex.trim();
        if ((s.length() & 1) != 0) {
            throw new InvalidSignatureDataException(fieldName + ": некорректная длина hex");
        }
        int n = s.length() / 2;
        byte[] out = new byte[n];
        for (int i = 0; i < n; i++) {
            int hi = Character.digit(s.charAt(i * 2), 16);
            int lo = Character.digit(s.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new InvalidSignatureDataException(fieldName + ": недопустимый символ в hex");
            }
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}
