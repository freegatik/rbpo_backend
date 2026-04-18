package ru.rbpo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rbpo.backend.dto.*;
import ru.rbpo.backend.exception.LicenseConflictException;
import ru.rbpo.backend.exception.LicenseForbiddenException;
import ru.rbpo.backend.exception.ResourceNotFoundException;
import ru.rbpo.backend.model.*;
import ru.rbpo.backend.repository.*;
import ru.rbpo.backend.signature.SignatureService;

import java.time.Instant;
import java.util.UUID;

/** Лицензии: создание, активация по ключу+устройству, проверка по device+product, продление. Тикет с ЭЦП через SignatureService. */
@Service
public class LicenseService {

    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final LicenseRepository licenseRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryRepository licenseHistoryRepository;
    private final SignatureService signatureService;

    @Value("${license.ticket-ttl-seconds:3600}")
    private long ticketTtlSeconds;

    /** Продление разрешено за 7 дней до истечения или при неактивной лицензии (методичка). */
    private static final int RENEW_WITHIN_DAYS = 7;

    public LicenseService(ProductRepository productRepository,
                          LicenseTypeRepository licenseTypeRepository,
                          LicenseRepository licenseRepository,
                          UserRepository userRepository,
                          DeviceRepository deviceRepository,
                          DeviceLicenseRepository deviceLicenseRepository,
                          LicenseHistoryRepository licenseHistoryRepository,
                          SignatureService signatureService) {
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.licenseRepository = licenseRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.deviceLicenseRepository = deviceLicenseRepository;
        this.licenseHistoryRepository = licenseHistoryRepository;
        this.signatureService = signatureService;
    }

    @Transactional
    public License createLicense(CreateLicenseRequest request, Long adminId) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Продукт не найден"));
        if (product.isBlocked()) {
            throw new LicenseConflictException("Продукт заблокирован");
        }

        LicenseType type = licenseTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Тип лицензии не найден"));

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Владелец лицензии не найден"));

        License license = new License();
        license.setCode(generateCode());
        license.setProduct(product);
        license.setType(type);
        license.setOwner(owner);
        license.setUser(null);
        license.setFirstActivationDate(null);
        license.setEndingDate(null);
        license.setBlocked(false);
        license.setDeviceCount(Math.max(1, request.getDeviceCount()));
        license.setDescription(request.getDescription());

        license = licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(userRepository.getReferenceById(adminId));
        history.setStatus(LicenseHistoryStatus.CREATED);
        history.setChangeDate(Instant.now());
        history.setDescription("Создана администратором");
        licenseHistoryRepository.save(history);

        return license;
    }

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Лицензия не найдена"));

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new LicenseForbiddenException("Лицензия принадлежит другому пользователю");
        }

        if (license.getProduct().isBlocked() || license.isBlocked()) {
            throw new LicenseConflictException("Лицензия или продукт заблокированы");
        }

        Device device = deviceRepository.findByMacAddress(request.getDeviceMac().trim())
                .orElseGet(() -> {
                    Device d = new Device();
                    d.setUser(userRepository.getReferenceById(userId));
                    d.setMacAddress(request.getDeviceMac().trim());
                    d.setName(request.getDeviceName() != null ? request.getDeviceName() : request.getDeviceMac());
                    return deviceRepository.save(d);
                });

        if (!device.getUser().getId().equals(userId)) {
            throw new LicenseForbiddenException("Устройство принадлежит другому пользователю");
        }

        boolean firstActivation = isFirstActivation(license);

        if (firstActivation) {
            license.setUser(userRepository.getReferenceById(userId));
            license.setFirstActivationDate(Instant.now());
            int days = license.getType().getDefaultDurationInDays() != null
                    ? license.getType().getDefaultDurationInDays() : 365;
            license.setEndingDate(Instant.now().plusSeconds(days * 86400L));
            licenseRepository.save(license);
        } else {
            int count = deviceLicenseRepository.countByLicense(license);
            if (count >= license.getDeviceCount()) {
                throw new LicenseConflictException("Достигнут лимит устройств");
            }
        }

        if (deviceLicenseRepository.findByLicenseAndDevice(license, device).isEmpty()) {
            DeviceLicense dl = new DeviceLicense();
            dl.setLicense(license);
            dl.setDevice(device);
            dl.setActivationDate(Instant.now());
            deviceLicenseRepository.save(dl);
        }

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(userRepository.getReferenceById(userId));
        history.setStatus(LicenseHistoryStatus.ACTIVATED);
        history.setChangeDate(Instant.now());
        history.setDescription(firstActivation ? "Первая активация" : "Активация на устройстве");
        licenseHistoryRepository.save(history);

        license = licenseRepository.findById(license.getId()).orElseThrow();
        return buildTicketResponse(license, device);
    }

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(CheckLicenseRequest request, Long userId) {
        Device device = deviceRepository.findByMacAddress(request.getDeviceMac().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Устройство не найдено"));

        if (!device.getUser().getId().equals(userId)) {
            throw new LicenseForbiddenException("Устройство принадлежит другому пользователю");
        }

        License license = licenseRepository.findActiveByDeviceUserAndProduct(
                        device, userId, request.getProductId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Активная лицензия не найдена"));

        return buildTicketResponse(license, device);
    }

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) {
        License license = licenseRepository.findByCode(request.getActivationKey().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Лицензия не найдена"));

        if (license.getUser() == null || !license.getUser().getId().equals(userId)) {
            throw new LicenseForbiddenException("Лицензия не принадлежит пользователю");
        }

        if (license.getProduct().isBlocked() || license.isBlocked()) {
            throw new LicenseConflictException("Лицензия или продукт заблокированы");
        }

        if (license.getFirstActivationDate() == null) {
            throw new LicenseConflictException("Сначала активируйте лицензию (нет даты первой активации).");
        }

        Instant now = Instant.now();
        Instant endingBefore = license.getEndingDate();
        int days = license.getType().getDefaultDurationInDays() != null
                ? license.getType().getDefaultDurationInDays() : 365;
        long durationSeconds = days * 86400L;

        if (endingBefore == null) {
            license.setEndingDate(now.plusSeconds(durationSeconds));
        } else {
            long daysUntilExpiry = (endingBefore.getEpochSecond() - now.getEpochSecond()) / 86400;
            if (daysUntilExpiry > RENEW_WITHIN_DAYS) {
                throw new LicenseConflictException(
                        "Продление возможно не ранее чем за " + RENEW_WITHIN_DAYS + " дней до истечения. Сейчас до истечения: " + daysUntilExpiry + " дн.");
            }
            Instant newEnding = endingBefore.isAfter(now)
                    ? endingBefore.plusSeconds(durationSeconds)
                    : now.plusSeconds(durationSeconds);
            license.setEndingDate(newEnding);
        }
        licenseRepository.save(license);

        LicenseHistory history = new LicenseHistory();
        history.setLicense(license);
        history.setUser(userRepository.getReferenceById(userId));
        history.setStatus(LicenseHistoryStatus.RENEWED);
        history.setChangeDate(Instant.now());
        history.setDescription(endingBefore == null
                ? "Продление: выставлена ending_date (ранее была null)"
                : "Продление лицензии");
        licenseHistoryRepository.save(history);

        return buildTicketResponse(license, null);
    }

    /** Первая активация: после создания лицензии поле user ещё null (не привязан активировавший пользователь). */
    private static boolean isFirstActivation(License license) {
        return license.getUser() == null;
    }

    private TicketResponse buildTicketResponse(License license, Device device) {
        Ticket ticket = new Ticket();
        Instant now = Instant.now();
        ticket.setServerDate(now);
        ticket.setTtlSeconds(ticketTtlSeconds);
        ticket.setActivationDate(license.getFirstActivationDate());
        ticket.setExpiryDate(license.getEndingDate());
        ticket.setUserId(license.getUser() != null ? license.getUser().getId() : null);
        ticket.setDeviceId(device != null ? device.getId() : null);
        ticket.setBlocked(license.isBlocked());
        String signature = signatureService.signTicket(ticket);
        return new TicketResponse(ticket, signature);
    }

    private static String generateCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase();
    }
}
