package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.Device;
import ru.rbpo.backend.model.License;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByCode(String code);

    /** Активные лицензии по устройству, пользователю и продукту (может быть несколько — берём с максимальным сроком). */
    @Query("SELECT l FROM License l " +
           "JOIN DeviceLicense dl ON dl.license = l AND dl.device = :device " +
           "WHERE l.user.id = :userId AND l.product.id = :productId " +
           "AND l.blocked = false AND l.endingDate >= CURRENT_TIMESTAMP " +
           "ORDER BY l.endingDate DESC")
    List<License> findActiveByDeviceUserAndProduct(
            @Param("device") Device device,
            @Param("userId") Long userId,
            @Param("productId") Long productId);
}
