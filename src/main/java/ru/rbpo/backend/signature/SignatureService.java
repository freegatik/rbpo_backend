package ru.rbpo.backend.signature;

import org.springframework.stereotype.Service;
import ru.rbpo.backend.dto.Ticket;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сервис ЭЦП: канонизация payload по RFC 8785 и подпись алгоритмом SHA256withRSA.
 * Результат подписи возвращается в Base64.
 */
@Service
public class SignatureService {

    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private final SignatureKeyStoreLoader keyStoreLoader;

    public SignatureService(SignatureKeyStoreLoader keyStoreLoader) {
        this.keyStoreLoader = keyStoreLoader;
    }

    /**
     * Подписывает тикет лицензии: приводится к каноническому JSON, подпись SHA256withRSA, результат Base64.
     */
    public String signTicket(Ticket ticket) {
        Map<String, Object> payload = ticketToMap(ticket);
        return sign(payload);
    }

    /**
     * Подписывает произвольный payload (Map). Канонический JSON в UTF-8 → SHA256withRSA → Base64.
     */
    public String sign(Map<String, Object> payload) {
        String canonical = JsonCanonicalizer.toCanonicalString(payload);
        byte[] utf8 = canonical.getBytes(StandardCharsets.UTF_8);
        try {
            PrivateKey key = keyStoreLoader.getPrivateKey();
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(key);
            sig.update(utf8);
            byte[] signatureBytes = sig.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка подписи: " + e.getMessage(), e);
        }
    }

    /** Преобразование тикета в Map с фиксированным набором полей для канонизации. */
    private static Map<String, Object> ticketToMap(Ticket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("activationDate", t.getActivationDate() != null ? t.getActivationDate().toString() : null);
        m.put("blocked", t.isBlocked());
        m.put("deviceId", t.getDeviceId());
        m.put("expiryDate", t.getExpiryDate() != null ? t.getExpiryDate().toString() : null);
        m.put("serverDate", t.getServerDate() != null ? t.getServerDate().toString() : null);
        m.put("ttlSeconds", t.getTtlSeconds());
        m.put("userId", t.getUserId());
        return m;
    }
}
