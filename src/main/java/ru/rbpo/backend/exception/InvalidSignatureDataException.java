package ru.rbpo.backend.exception;

/** Невалидные данные сигнатуры/запроса → 400 в GlobalExceptionHandler. */
public class InvalidSignatureDataException extends RuntimeException {

    public InvalidSignatureDataException(String message) {
        super(message);
    }

    public InvalidSignatureDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
