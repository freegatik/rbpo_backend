package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.Device;

import java.util.Optional;

/** device: по id, по mac_address. */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByMacAddress(String macAddress);
}
