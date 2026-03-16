package ru.rbpo.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import ru.rbpo.backend.dto.*;
import ru.rbpo.backend.model.License;
import ru.rbpo.backend.security.CurrentUserProvider;
import ru.rbpo.backend.service.LicenseService;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;
    private final CurrentUserProvider currentUserProvider;

    public LicenseController(LicenseService licenseService, CurrentUserProvider currentUserProvider) {
        this.licenseService = licenseService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<LicenseCreatedResponse> createLicense(@Valid @RequestBody CreateLicenseRequest request) {
        var user = currentUserProvider.getCurrentUser();
        if (user == null || user.getRole() != ru.rbpo.backend.model.Role.ADMIN) {
            throw new AccessDeniedException("Только администратор может создавать лицензии");
        }
        License license = licenseService.createLicense(request, user.getId());
        return new ResponseEntity<>(LicenseCreatedResponse.from(license), HttpStatus.CREATED);
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activateLicense(@Valid @RequestBody ActivateLicenseRequest request) {
        var user = currentUserProvider.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
        return ResponseEntity.ok(licenseService.activateLicense(request, user.getId()));
    }

    @PostMapping("/check")
    public ResponseEntity<TicketResponse> checkLicense(@Valid @RequestBody CheckLicenseRequest request) {
        var user = currentUserProvider.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
        return ResponseEntity.ok(licenseService.checkLicense(request, user.getId()));
    }

    @PostMapping("/renew")
    public ResponseEntity<TicketResponse> renewLicense(@Valid @RequestBody RenewLicenseRequest request) {
        var user = currentUserProvider.getCurrentUser();
        if (user == null) {
            throw new AccessDeniedException("Требуется аутентификация");
        }
        return ResponseEntity.ok(licenseService.renewLicense(request, user.getId()));
    }
}
