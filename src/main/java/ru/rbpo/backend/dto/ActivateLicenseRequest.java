package ru.rbpo.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** Тело POST /api/licenses/activate (activationKey, deviceMac, deviceName?). */
public class ActivateLicenseRequest {

    @NotBlank(message = "Ключ активации обязателен")
    private String activationKey;

    @NotBlank(message = "MAC-адрес устройства обязателен")
    private String deviceMac;

    private String deviceName;

    public String getActivationKey() { return activationKey; }
    public void setActivationKey(String activationKey) { this.activationKey = activationKey; }
    public String getDeviceMac() { return deviceMac; }
    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
}
