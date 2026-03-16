package ru.rbpo.backend.exception;

/** Конфликт (например, лимит устройств при активации) → 409. */
public class LicenseConflictException extends RuntimeException {

    public LicenseConflictException(String message) {
        super(message);
    }
}
