package ru.rbpo.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Сессия пользователя (refresh token). Таблица user_sessions. */
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userEmail;

    private String deviceId;

    @Column(length = 512)
    private String accessToken;

    @Column(length = 512, nullable = false)
    private String refreshToken;

    private Instant accessTokenExpiry;

    @Column(nullable = false)
    private Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    public UserSession() {
    }

    public UserSession(String userEmail, String deviceId, String accessToken, String refreshToken,
                       Instant accessTokenExpiry, Instant refreshTokenExpiry, SessionStatus status) {
        this.userEmail = userEmail;
        this.deviceId = deviceId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.status = status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Instant getAccessTokenExpiry() { return accessTokenExpiry; }
    public void setAccessTokenExpiry(Instant accessTokenExpiry) { this.accessTokenExpiry = accessTokenExpiry; }
    public Instant getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(Instant refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
}
