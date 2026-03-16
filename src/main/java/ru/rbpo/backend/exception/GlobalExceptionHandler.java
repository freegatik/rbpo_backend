package ru.rbpo.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            errors.put(field, error.getDefaultMessage());
        });
        ValidationErrorResponse body = new ValidationErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Ошибка валидации", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Не найдено", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LicenseForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleLicenseForbidden(LicenseForbiddenException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), "Доступ запрещён", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LicenseConflictException.class)
    public ResponseEntity<ErrorResponse> handleLicenseConflict(LicenseConflictException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.CONFLICT.value(), "Конфликт", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Ошибка токена",
                ex.getMessage() != null ? ex.getMessage() : "Невалидный или истекший токен");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), "Доступ запрещён",
                ex.getMessage() != null ? ex.getMessage() : "Недостаточно прав");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), "Ошибка аутентификации",
                ex.getMessage() != null ? ex.getMessage() : "Требуется аутентификация");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Ошибка", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Внутренняя ошибка сервера",
                ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;

        public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
        }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
    }

    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private Map<String, String> errors;

        public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, Map<String, String> errors) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.errors = errors;
        }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public Map<String, String> getErrors() { return errors; }
    }
}
