package ru.rbpo.backend.exception;

/** Невалидный или истёкший токен → 401. */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }
}
