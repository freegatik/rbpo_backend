package ru.rbpo.backend.exception;

public class LicenseForbiddenException extends RuntimeException {

    public LicenseForbiddenException(String message) {
        super(message);
    }
}
