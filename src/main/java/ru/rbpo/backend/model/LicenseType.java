package ru.rbpo.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Тип лицензии (TRIAL, MONTH, YEAR и т.д.), длительность по умолчанию. Таблица license_type. */
@Entity
@Table(name = "license_type")
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(name = "default_duration_in_days", nullable = false)
    private Integer defaultDurationInDays;

    @Column(columnDefinition = "text")
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDefaultDurationInDays() { return defaultDurationInDays; }
    public void setDefaultDurationInDays(Integer defaultDurationInDays) { this.defaultDurationInDays = defaultDurationInDays; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
