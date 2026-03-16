package ru.rbpo.backend.model;

/** Состояние сессии (user_sessions): активна, использована при refresh, отозвана. */
public enum SessionStatus {
    ACTIVE,
    USED,
    REVOKED
}
