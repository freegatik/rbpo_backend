package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.LicenseType;

/** Справочник license_type. */
@Repository
public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
}
