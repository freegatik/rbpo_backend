package ru.rbpo.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** Устройство пользователя (MAC, имя). Таблица device. */
@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "mac_address", nullable = false, unique = true, length = 64)
    private String macAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
