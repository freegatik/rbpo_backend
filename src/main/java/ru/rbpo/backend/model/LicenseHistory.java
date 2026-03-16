package ru.rbpo.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "license_history")
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseHistoryStatus status;

    @Column(name = "change_date", nullable = false)
    private Instant changeDate;

    @Column(columnDefinition = "text")
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public License getLicense() { return license; }
    public void setLicense(License license) { this.license = license; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LicenseHistoryStatus getStatus() { return status; }
    public void setStatus(LicenseHistoryStatus status) { this.status = status; }
    public Instant getChangeDate() { return changeDate; }
    public void setChangeDate(Instant changeDate) { this.changeDate = changeDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
