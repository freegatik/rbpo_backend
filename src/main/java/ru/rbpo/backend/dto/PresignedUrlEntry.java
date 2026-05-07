package ru.rbpo.backend.dto;

import java.util.UUID;

/** Элемент списка presigned URL (задание 6). */
public record PresignedUrlEntry(UUID signatureId, String presignedUrl) {}
