package ru.rbpo.backend.exception;

/** Доступ к лицензии запрещён (например, активирована другим пользователем) → 403. */
public class LicenseForbiddenException extends RuntimeException {

    public LicenseForbiddenException(String message) {
        super(message);
    }
}
