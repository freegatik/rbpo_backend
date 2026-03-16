package ru.rbpo.backend.exception;

/** Ресурс не найден → 404 в GlobalExceptionHandler. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
