package ru.rbpo.backend.exception;

/**
 * Невалидные данные сигнатуры — возвращаем 400 Bad Request.
 */
public class InvalidSignatureDataException extends RuntimeException {

    public InvalidSignatureDataException(String message) {
        super(message);
    }

    public InvalidSignatureDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
