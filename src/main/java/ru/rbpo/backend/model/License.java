package ru.rbpo.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Лицензия (активационный ключ, продукт, тип, владелец, даты, лимит устройств). Таблица license. */
@Entity
@Table(name = "license")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Кто активировал; null = ещё ни разу не активировали (только owner при создании). Первая активация = это поле было null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_activation_date")
    private Instant firstActivationDate;

    /** null до первой активации; после активации обычно задана. Если null при уже заданном user — продление выставляет срок заново. */
    @Column(name = "ending_date")
    private Instant endingDate;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(name = "device_count", nullable = false)
    private int deviceCount = 1;

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "license", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceLicense> deviceLicenses = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public LicenseType getType() { return type; }
    public void setType(LicenseType type) { this.type = type; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Instant getFirstActivationDate() { return firstActivationDate; }
    public void setFirstActivationDate(Instant firstActivationDate) { this.firstActivationDate = firstActivationDate; }
    public Instant getEndingDate() { return endingDate; }
    public void setEndingDate(Instant endingDate) { this.endingDate = endingDate; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<DeviceLicense> getDeviceLicenses() { return deviceLicenses; }
}
