package ru.rbpo.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Тело POST /api/licenses (productId, typeId, ownerId, deviceCount?, description?). */
public class CreateLicenseRequest {

    @NotNull(message = "productId обязателен")
    private Long productId;

    @NotNull(message = "typeId обязателен")
    private Long typeId;

    @NotNull(message = "ownerId обязателен")
    private Long ownerId;

    @Min(1)
    private int deviceCount = 1;

    private String description;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getTypeId() { return typeId; }
    public void setTypeId(Long typeId) { this.typeId = typeId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
