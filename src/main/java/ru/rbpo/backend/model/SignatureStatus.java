package ru.rbpo.backend.model;

/**
 * Статус записи антивирусной сигнатуры.
 * ACTUAL — актуальная запись, DELETED — логически удалённая.
 */
public enum SignatureStatus {
    ACTUAL,
    DELETED
}
