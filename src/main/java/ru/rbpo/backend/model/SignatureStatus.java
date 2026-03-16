package ru.rbpo.backend.model;

/** ACTUAL — в полной выгрузке; DELETED — только в инкременте, не удаляем из БД. */
public enum SignatureStatus {
    ACTUAL,
    DELETED
}
