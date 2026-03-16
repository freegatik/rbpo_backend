package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.LicenseHistory;

@Repository
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
}
