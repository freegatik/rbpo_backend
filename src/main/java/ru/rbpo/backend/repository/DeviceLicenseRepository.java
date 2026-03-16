package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.Device;
import ru.rbpo.backend.model.DeviceLicense;
import ru.rbpo.backend.model.License;

import java.util.Optional;

/** device_license: связь лицензия–устройство, проверка дубля. */
@Repository
public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {

    @Query("SELECT COUNT(dl) FROM DeviceLicense dl WHERE dl.license = :license")
    int countByLicense(@Param("license") License license);

    Optional<DeviceLicense> findByLicenseAndDevice(License license, Device device);
}
