package ru.rbpo.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Проверка сложности пароля для аннотации @StrongPassword. */
public class PasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) return false;
        if (password.length() < 8) return false;
        if (!password.chars().anyMatch(Character::isDigit)) return false;
        if (!password.chars().anyMatch(Character::isLetter)) return false;
        if (!password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))) return false;
        return true;
    }
}
