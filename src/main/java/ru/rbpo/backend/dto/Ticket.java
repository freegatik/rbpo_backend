package ru.rbpo.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * Тикет для передачи информации о лицензии клиентам.
 */
public class Ticket {

    /** Текущая дата/время сервера */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant serverDate;

    /** Время жизни тикета (секунды) */
    private long ttlSeconds;

    /** Дата первой активации лицензии */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant activationDate;

    /** Дата истечения лицензии */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiryDate;

    /** Идентификатор пользователя */
    private Long userId;

    /** Идентификатор устройства */
    private Long deviceId;

    /** Флаг блокировки лицензии */
    private boolean blocked;

    public Ticket() {
    }

    public Instant getServerDate() { return serverDate; }
    public void setServerDate(Instant serverDate) { this.serverDate = serverDate; }
    public long getTtlSeconds() { return ttlSeconds; }
    public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }
    public Instant getActivationDate() { return activationDate; }
    public void setActivationDate(Instant activationDate) { this.activationDate = activationDate; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
