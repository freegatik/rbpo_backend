package ru.rbpo.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Тело POST /api/auth/refresh (refreshToken). */
public class RefreshRequest {

    @NotBlank(message = "Refresh токен не может быть пустым")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
