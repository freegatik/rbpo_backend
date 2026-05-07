package ru.rbpo.backend.binary;

import java.util.List;

/** Результат сборки data.bin: полный файл, SHA-256, длины записей в области payload (после заголовка). */
public record DataBinBuildResult(byte[] fullBytes, byte[] sha256, List<Integer> recordPayloadLengths) {}
