package ru.rbpo.backend.exception;

public class LicenseConflictException extends RuntimeException {

    public LicenseConflictException(String message) {
        super(message);
    }
}
