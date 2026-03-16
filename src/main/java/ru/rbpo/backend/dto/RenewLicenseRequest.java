package ru.rbpo.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class RenewLicenseRequest {

    @NotBlank(message = "Ключ активации обязателен")
    private String activationKey;

    public String getActivationKey() { return activationKey; }
    public void setActivationKey(String activationKey) { this.activationKey = activationKey; }
}
