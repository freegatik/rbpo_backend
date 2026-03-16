package ru.rbpo.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Ограничение пароля: длина, цифра, буква, спецсимвол. Валидация в PasswordValidator. */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface StrongPassword {
    String message() default "Пароль должен содержать минимум 8 символов, хотя бы одну цифру, одну букву и один специальный символ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
