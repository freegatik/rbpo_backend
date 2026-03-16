package ru.rbpo.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Тело POST /api/licenses/renew (activationKey). */
public class RenewLicenseRequest {

    @NotBlank(message = "Ключ активации обязателен")
    private String activationKey;

    public String getActivationKey() { return activationKey; }
    public void setActivationKey(String activationKey) { this.activationKey = activationKey; }
}
