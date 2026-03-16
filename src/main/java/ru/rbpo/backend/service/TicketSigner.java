package ru.rbpo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.rbpo.backend.dto.Ticket;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Формирование ЭЦП тикета на основе HMAC-SHA256.
 */
@Component
public class TicketSigner {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final byte[] secret;

    public TicketSigner(@Value("${license.ticket-signature-secret:${jwt.secret:defaultTicketSigningSecret}}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Каноническое строковое представление тикета для подписи (порядок полей фиксирован).
     */
    public static String toCanonicalString(Ticket t) {
        return String.format("%s|%d|%s|%s|%s|%s|%s",
                t.getServerDate() != null ? t.getServerDate().toString() : "",
                t.getTtlSeconds(),
                t.getActivationDate() != null ? t.getActivationDate().toString() : "",
                t.getExpiryDate() != null ? t.getExpiryDate().toString() : "",
                t.getUserId() != null ? t.getUserId() : "",
                t.getDeviceId() != null ? t.getDeviceId() : "",
                t.isBlocked());
    }

    public String sign(Ticket ticket) {
        String payload = toCanonicalString(ticket);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Ошибка подписи тикета", e);
        }
    }
}
