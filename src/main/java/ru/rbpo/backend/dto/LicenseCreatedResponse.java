package ru.rbpo.backend.dto;

import ru.rbpo.backend.model.License;

/** Ответ 201 после POST /api/licenses. */
public class LicenseCreatedResponse {

    private Long id;
    private String code;
    private Long productId;
    private Long typeId;
    private Long ownerId;
    private int deviceCount;
    private String description;

    public static LicenseCreatedResponse from(License license) {
        LicenseCreatedResponse r = new LicenseCreatedResponse();
        r.setId(license.getId());
        r.setCode(license.getCode());
        r.setProductId(license.getProduct().getId());
        r.setTypeId(license.getType().getId());
        r.setOwnerId(license.getOwner().getId());
        r.setDeviceCount(license.getDeviceCount());
        r.setDescription(license.getDescription());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
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
