package ru.rbpo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckLicenseRequest {

    @NotBlank(message = "MAC-адрес устройства обязателен")
    private String deviceMac;

    @NotNull(message = "productId обязателен")
    private Long productId;

    public String getDeviceMac() { return deviceMac; }
    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
}
